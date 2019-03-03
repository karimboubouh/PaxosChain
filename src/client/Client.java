package client;

import core.Global;
import core.Host;
import core.Transaction;
import protocol.Protocol;
import protocol.Request;
import protocol.Response;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {

    // client identity
    private Host host;
    // estimated leader identity
    private Host leader = null;
    // list of known servers
    private List<Host> knownServers;
    // Communication socket
    private DatagramSocket udpSocket;
    // client transaction
    private Transaction transaction;

    public Client(Host host) throws IOException {
        this.host = host;
        init();
        start();

    }

    private void init() throws IOException {
        this.udpSocket = new DatagramSocket(host.getPort(), host.getAddress());
        this.knownServers = Global.servers();
        ReceiverThread receiver = new ReceiverThread(this);
        receiver.start();
    }

    private void start() throws IOException {
        // start a transaction request ...
        while(true){
            System.out.println("Press Enter to send 2 transactions");
            System.in.read();
            Request req1 = Protocol.addTransaction(host, new Transaction(1, 2, 140));
            Request req2 = Protocol.addTransaction(host, new Transaction(1, 2, 20));
            if (leader != null) {
                sendRequest(req1, leader);
                sendRequest(req2, leader);
            } else {
                broadcast(req1);
                broadcast(req2);
            }
            System.in.read();
        }
        /*
        while (true) {
            Runtime.getRuntime().exec("clear");
            System.out.print("\033[H\033[2J");
            System.out.flush();
            Scanner sc = new Scanner(System.in);
            System.out.println("\n-------------------- Client ID : " + host.getId() + " --------------------");
            System.out.println("* Check your wallet : 1");
            System.out.println("* Add a transaction : 2");
            System.out.println("* Terminate program : q");
            System.out.print(">> Choose an operation : ");
            String op = sc.nextLine().trim();
            switch (op) {
                case "1":
                    getBalance();
                    break;
                case "2":
                    doTransaction();
                    break;
                case "q":
                    stop();
                    break;
                default:
                    System.out.println("Please choose a valid option");
            }
            System.in.read();
        }
        */
    }

    private void getBalance() throws IOException {
        // Request client balance
        System.out.println("Requesting balance ...");
        if(leader != null){
            sendRequest(Protocol.getBalance(host), leader);
        }else{
            sendRequest(Protocol.getBalance(host), this.knownServers.get(0));
        }
    }

    private void doTransaction() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Set your transaction in the format <Sender Receiver Amount> : ");
        try {
            String[] tr = sc.nextLine().trim().split(" ");
            int senderId = Integer.parseInt(tr[0]);
            int receiverId = Integer.parseInt(tr[1]);
            float amount = Float.parseFloat(tr[2]);
            if (senderId != host.getId()) throw new Exception();
            Request request = Protocol.addTransaction(host, new Transaction(senderId, receiverId, amount));
            if (leader != null) {
                sendRequest(request, leader);
            } else {
                broadcast(request);
            }

        } catch (Exception e) {
            System.out.println("Wrong transaction !");
            e.printStackTrace();
        }

    }

    private void stop() {
        System.out.println("Client exiting ...");
        System.exit(0);
    }

    @Override
    public String toString() {
        return "Client{" + host.getId() + ", " + host.getPort() + "}";
    }

    private byte[] objectToByte(Object o) throws IOException {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(bStream);
        oo.writeObject(o);
        oo.close();
        return bStream.toByteArray();
    }

    private Object byteToObject(byte[] b) throws IOException, ClassNotFoundException {
        ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(b));
        Object o = iStream.readObject();
        iStream.close();
        return o;
    }

    private void sendRequest(Request request, Host server) throws IOException {
        // Send client requests
        byte[] buf = objectToByte(request);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, server.getAddress(), server.getPort());
        udpSocket.send(packet);
        System.out.println("Request sent to " + server + " ...");
    }

    private void broadcast(Request request) throws IOException {
        System.out.println("Boadcasting request to all servers ...");
        for (Host server : this.knownServers) {
            sendRequest(request, server);
        }
    }

    private class ReceiverThread extends Thread {

        Client client;
        private boolean keepGoing = true;

        ReceiverThread(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                while (keepGoing) {
                    byte[] buf = new byte[2048];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    udpSocket.receive(packet);
                    Response response = (Response) byteToObject(packet.getData());

                    switch (response.getType()) {
                        case Protocol.SEND_BALANCE:
                            System.out.println(response.getMessage());
                            break;
                        case Protocol.SEND_BALANCE_ERROR:
                            System.out.println("We couldn't retrieve your balance : " + response.getMessage() + "!");
                            break;

                        case Protocol.SEND_TRANSACTION_STATUS:
                            System.out.println(response.getMessage() + " with status :" + response.getStatus());
                            break;
                        case Protocol.SEND_TRANSACTION_STATUS_ERROR:
                            System.out.println(response.getMessage());
                            break;
                        default:
                            System.out.println("Unknown message received !");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int id = Integer.parseInt(args[0]);
        new Client(Global.clients().get(id-1));
    }
}
