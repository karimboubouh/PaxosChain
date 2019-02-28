package client;

import core.Transaction;
import protocol.Protocol;
import protocol.Request;
import protocol.Response;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {

    // Client global variables
    public static final int CLIENT_ID = 1;
    public static String CLIENT_HOST = "localhost";
    public static int CLIENT_PORT = 9065;

    // estimated leader identity
    private List<Integer> leader = null;

    // Communication socket
    private List<List<Integer>> knwonServers;
    private DatagramSocket udpSocket;

    // client attributes
    private Transaction transaction;


    public Client() throws IOException {
        init();
        start();
    }

    private void init() throws IOException {
        // init DatagramSocket
        this.udpSocket = new DatagramSocket(CLIENT_PORT, InetAddress.getByName(CLIENT_HOST));
        this.knwonServers = new ArrayList<>();
        this.knwonServers.add(Arrays.asList(1, 7501));
        this.knwonServers.add(Arrays.asList(2, 7502));
        this.knwonServers.add(Arrays.asList(3, 7503));
        // init client info
    }

    private void start() throws IOException {
        // start a transaction request ...
        while (true) {
            Runtime.getRuntime().exec("clear");
            Scanner sc = new Scanner(System.in);
            System.out.println("\n-------------------- Client ID : " + CLIENT_ID + " --------------------");
            System.out.println("* Check your wallet : 1");
            System.out.println("* Add a transaction : 2");
            System.out.println("* Terminate program : q");
            System.out.println(">> Choose an operation : ");
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
        }
    }

    private void getBalance() throws IOException {
        // Request client balance
        System.out.println("Requesting balance ...");
        if(leader != null){
            sendRequest(Protocol.getBalance(CLIENT_ID), leader);
        }else{
            sendRequest(Protocol.getBalance(CLIENT_ID), this.knwonServers.get(0));
        }
    }

    private void doTransaction() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Set your transaction in the format <Sender Receiver Amount> :");
        try {
            String[] tr = sc.nextLine().trim().split(" ");
            int sender_id = Integer.parseInt(tr[0]);
            int receiver_id = Integer.parseInt(tr[1]);
            float amount = Float.parseFloat(tr[2]);
            if (sender_id != CLIENT_ID) throw new Exception();
            Request request = Protocol.addTransaction(CLIENT_ID, new Transaction(sender_id, receiver_id, amount));
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
    }

    @Override
    public String toString() {
        return "Client{" + CLIENT_ID + "}";
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

    private void sendRequest(Request request, List<Integer> server) throws IOException {
        // Send client requests
        byte[] buf = objectToByte(request);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), server.get(1));
        udpSocket.send(packet);
        System.out.println("Request " + request.getType() + " has been sent ...");
    }

    private void broadcast(Request request) throws IOException {
        for (List<Integer> server : this.knwonServers) {
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
                            System.out.println("Your balance is : " + response.getMessage() + ".");
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
