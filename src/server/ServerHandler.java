package server;

import core.Block;
import core.Host;
import core.Paxos;
import core.Transaction;
import protocol.Protocol;
import protocol.Request;
import protocol.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import static server.Server.objectToByte;

public class ServerHandler extends Thread {

    private int id;
    private Server server;
    private Request request;
    private Response response;
    private int messageType;
    private InetAddress address;
    private int port;
    private Transaction balance;
    private ByteArrayOutputStream bos;
    private ObjectOutputStream oos;
    private DatagramSocket ds;
    // Constructor 

    public ServerHandler(Server server, Object message, InetAddress address, int port) {
        this.server = server;
        this.address = address;
        this.port = port;
        if (message instanceof Request) {
            this.request = (Request) message;
            this.messageType = 1;
        } else if (message instanceof Response) {
            this.response = (Response) message;
            this.messageType = 2;
        } else {
            System.out.println("Unknown message received!");
            Thread.interrupted();
        }
    }

    @Override
    public void run() {
        if (this.messageType == 1) {
            System.out.println("Request received!");
            handleRequest();
        } else if (this.messageType == 2) {
            System.out.println("Response received!");
            handleResponse();
        }
    }

    private void handleRequest() {
        try {
            switch (this.request.getType()) {
                case Protocol.GET_BALANCE:
                    sendBalance();
                    break;
                case Protocol.ADD_TRANSACTION:
                    server.getTransactions().add(request.getTransaction());
                    doElectionIfDeeded();
                    doTransaction();
                    break;
                case Protocol.PREPARE:
                    doPrepare();
                    break;
                case Protocol.PROPOSE:
                    doPropose();
                    break;
                case Protocol.DECIDE:
                    doDecide();
                    break;
                default:
                    System.out.println("we can't handle this type of request");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleResponse() {
        try {
            switch (this.response.getType()) {
                case Protocol.ACK:
                    doAck();
                    break;
                case Protocol.ACCEPT:
                    doAccept();
                    break;
                default:
                    System.out.println("we can't handle this type of response");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Paxos methods
    private void doElectionIfDeeded() throws IOException {
        if (server.getLeader() == null) {
            // No leader yet
            if(server.getTransactions().size() == 2){
                Paxos paxos = server.getPaxos();
                Request request = Protocol.sendPrepare(this.server.getHost(), this.server.getPaxos());
                this.server.broadcastRequest(request);
            }
        }
    }

    private void doPrepare() throws IOException {
        int[] ballot = request.getPaxos().getBallotNum();
        if (!server.getPaxos().checkBallotNumber(ballot)) {
            server.getPaxos().setBallotNum(ballot);
        }
        Response response = Protocol.sendAck(request.getId(), server.getHost(), server.getPaxos());
        sendResponse(response, request.getSender());
    }

    private void doAck() throws IOException {
        server.newAck(this.response.getPaxos());
    }

    private void doPropose() throws IOException {
        if (this.server.getPaxos().checkBallotNumber(this.request.getPaxos().getBallotNum())) {
            this.server.getPaxos().setAcceptNum(this.request.getPaxos().getBallotNum());
            this.server.getPaxos().setClientVal(this.request.getPaxos().getClientVal());
            Response response = Protocol.sendAccept(request.getId(), server.getHost(), server.getPaxos());
            sendResponse(response, request.getSender());
        }
    }

    private void doAccept() throws IOException {
        server.newAccept(this.response.getPaxos());
    }

    private void doDecide() {
        // TODO add Block to blockchain
        this.server.getBlockChain().addBlock(this.server.getPaxos().getAcceptVal());
    }

    private void sendBalance() throws IOException {
        // TODO Check if request already executed and inform client with it status
        String message = "Your balance is " + server.getUserBalance(this.request.getSender()) + "DH.";
        Response response = Protocol.sendBalance(this.request.getId(), this.server.getHost(), message);
        sendResponse(response, request.getSender());
    }

    private void doTransaction() throws IOException {
        // do propose the value if server is leader
        if (server.getLeader() != null && this.server.getHost().getId() == server.getLeader().getId()){
            // i'm the leader
            if(server.getTransactions().size() == 2) {
                Block block = this.server.getBlockChain().newBlock(this.server.getTransactions());
                this.server.getPaxos().setClientVal(block);
                Request request = Protocol.sendPropose(this.server.getHost(), this.server.getPaxos());
                this.server.broadcastRequest(request);
                server.setTransactions(new ArrayList<>());
            }
        }else{
            // transfer the request to the leader
            //this.server.sendRequest(request, this.server.getLeader());
            // TODO notify the client of the leader id
        }
    }

    private void sendResponse(Response response, Host receiver) throws IOException {
        // Send server responses
        byte[] buf = objectToByte(response);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, receiver.getAddress(), receiver.getPort());
        server.sendPacket(packet);
        System.out.println("Response " + response.getType() + " has been sent ...");
    }
}
