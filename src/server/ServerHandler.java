package server;

import core.Block;
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

import static server.Server.SERVER_ID;
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
            handleRequest();
        } else if (this.messageType == 2) {
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
                    doElectionIfDeeded();
                    //doTransaction(request);
                    break;
                case Protocol.PREPARE:
                    doPrepare();
                    break;
                case Protocol.PROPOSE:
                    doPropose();
                    break;
                    // decide
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
                case Protocol.DECIDE:
                    doDecide();
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
            server.getPaxos().doElection();
            Paxos paxos = server.getPaxos();
        }else{
            if (this.server.SERVER_ID == server.getLeader().get(0)){
                // i'm the leader
                // TODO Mine block using request.getTransaction()
                Block block = new Block();
                this.server.getPaxos().setClientVal(block);
                Request request = Protocol.sendPropose(SERVER_ID, this.server.getPaxos());
                this.server.broadcastRequest(request);
            }else{
                // transfer the request to the leader
                this.server.sendRequest(request, this.server.getLeader());
                // TODO notify the client of the leader id
            }
        }
    }

    private void doPrepare() throws IOException {
        int[] ballot = request.getPaxos().getBallotNum();
        if (!server.getPaxos().checkBallotNumber(ballot)) {
            server.getPaxos().setBallotNum(ballot);
        }
        Response response = Protocol.sendAck(request.getId(), server.SERVER_ID, server.getPaxos());
        sendResponse(response);
    }

    private void doAck() throws IOException {
        server.newAck(this.response.getPaxos());
    }

    private void doPropose() throws IOException {
        if (this.server.getPaxos().checkBallotNumber(this.request.getPaxos().getBallotNum())) {
            this.server.getPaxos().setAcceptNum(this.request.getPaxos().getBallotNum());
            this.server.getPaxos().setClientVal(this.request.getPaxos().getClientVal());
            Response response = Protocol.sendAccept(request.getId(), server.SERVER_ID, server.getPaxos());
            sendResponse(response);
        }
    }

    private void doAccept() throws IOException {
        server.newAccept(this.response.getPaxos());
    }

    private void doDecide() {
        // add Block to blockchain
    }

    private void sendBalance() throws IOException {
        // Check if request already executed and inform client with it status
        String message = "Your balance is " + server.getUserBalance(this.request.getSender()) + "DH.";
        Response response = Protocol.sendBalance(this.request.getId(), SERVER_ID, message);
        sendResponse(response);
    }

    private void doTransaction(Request request) {
        // create new block if buffer full (2tr)
        Block currentBlock = Blockchain.MineBlock(server.getTransactions());
        Block agreedBlock = server.getPaxos().agreeOnBlock(currentBlock);
        // update balance
        server.updateBalance(request.getTransaction());
    }

    private void sendResponse(Response response) throws IOException {
        // Send server responses
        byte[] buf = objectToByte(response);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        server.sendPacket(packet);
        System.out.println("Response " + response.getType() + " has been sent ...");
    }

    private void broadcast(Response response) throws IOException {
        // Send server responses
        byte[] buf = objectToByte(response);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        server.sendPacket(packet);
        System.out.println("Response " + response.getType() + " has been sent ...");
    }

}
