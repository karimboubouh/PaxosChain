package server;

import core.Block;
import core.Paxos;
import core.Transaction;
import protocol.Protocol;
import protocol.Request;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class Server {
    // Server global variables
    public static final int SERVER_ID = 1;
    public static String SERVER_HOST = "localhost";
    public static int SERVER_PORT = 8865;
    public static int MAJORITY = 2;

    // Communication socket
    private List<List<Integer>> knwonServers;
    private DatagramSocket udpSocket;
    private List<ServerHandler> workers;

    // server attributes
    private List<Integer> leader = null;
    private Blockchain blockchain;
    private Paxos paxos;
    private List<Transaction> transactions;
    private Map<Integer, Float> balances = new HashMap<>();

    // Paxos attributes
    private Map<int[], List<Paxos>> acks;
    private Map<int[], List<Paxos>> accepts;


    public Server() throws IOException, ClassNotFoundException {
        init();
        start();
    }

    public static byte[] objectToByte(Object o) throws IOException {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(bStream);
        oo.writeObject(o);
        oo.close();
        return bStream.toByteArray();
    }

    public static Object byteToObject(byte[] b) throws IOException, ClassNotFoundException {
        ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(b));
        Object o = iStream.readObject();
        iStream.close();
        return o;
    }

    private void init() throws IOException {
        // init DatagramSocket
        this.knwonServers = new ArrayList<>();
        this.knwonServers.add(Arrays.asList(1, 7501));
        this.knwonServers.add(Arrays.asList(2, 7502));
        this.knwonServers.add(Arrays.asList(3, 7503));
        MAJORITY = this.knwonServers.size();
        this.udpSocket = new DatagramSocket(SERVER_PORT, InetAddress.getByName(SERVER_HOST));
        this.workers = new ArrayList<>();

        // init server info
        this.blockchain = new Blockchain();
        this.paxos = new Paxos(SERVER_ID);
        this.transactions = new ArrayList<>();
        this.balances = new HashMap<>();

        // init paxos attributes
        this.acks = new HashMap<>();
        this.accepts = new HashMap<>();

        // user balances
        balances.put(1, 100f);
        balances.put(2, 200f);
        balances.put(3, 150f);

    }

    public void start() throws IOException, ClassNotFoundException {
        System.out.println(" server started");
        while (true) {
            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            udpSocket.receive(packet);
            Object message = (Object) byteToObject(packet.getData());
            workers.add(new ServerHandler(this, message, packet.getAddress(), packet.getPort()));
        }
    }

    public Float getUserBalance(int userId) {
        return balances.get(userId);
    }

    public Map<Integer, Float> getBalances() {
        return this.balances;
    }

    public void sendPacket(DatagramPacket packet) throws IOException {
        udpSocket.send(packet);
    }

    public void updateBalance(Transaction transaction) {
        int senderID = transaction.getSenderId();
        int receiverID = transaction.getReceiverId();
        float amount = transaction.getAmount();
        balances.put(senderID, balances.get(senderID) - amount);
        balances.put(receiverID, balances.get(receiverID) + amount);
    }

    public List<Integer> getLeader() {
        return leader;
    }

    public void setLeader(List<Integer> leader) {
        this.leader = leader;
    }

    public Paxos getPaxos() {
        return paxos;
    }

    public void setPaxos(Paxos paxos) {
        this.paxos = paxos;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }


    /*
     * Paxos receive ACK : receive ack messages and add them to a buffer.
     * Once you have a majority of acks check for the val of the highest
     * ballot number and retrive its value or use client value.
     * Then send Accept message to all
     */
    public void newAck(Paxos receivedPaxos) throws IOException {
        int[] b = receivedPaxos.getBallotNum();
        Block val = this.paxos.getClientVal();
        if (acks.containsKey(b)) {
            acks.get(b).add(receivedPaxos);
        } else {
            List<Paxos> p = new ArrayList<>();
            p.add(receivedPaxos);
            acks.put(b, p);
        }
        if (acks.get(b).size() >= MAJORITY) {
            // to check
            int[] ballot = this.paxos.getBallotNum();
            for (Paxos pax : acks.get(b)) {
                if (pax.compareAcceptNum(ballot)) {
                    ballot = pax.getAcceptNum();
                    if (pax.getAcceptVal() != null) {
                        val = pax.getAcceptVal();
                    }
                }
            }
            this.paxos.setClientVal(val);
            Request request = Protocol.sendPropose(SERVER_ID, this.paxos);
            broadcastRequest(request);
        }
    }

    public void newAccept(Paxos receivedPaxos) throws IOException {
        int[] b = receivedPaxos.getBallotNum();
        if (accepts.containsKey(b)) {
            accepts.get(b).add(receivedPaxos);
        } else {
            List<Paxos> p = new ArrayList<>();
            p.add(receivedPaxos);
            accepts.put(b, p);
        }
        if (accepts.get(b).size() >= MAJORITY) {
            Request request = Protocol.sendDecide(SERVER_ID, this.paxos);
            broadcastRequest(request);
        }
    }


    public void broadcastRequest(Request request) throws IOException {
        for (List<Integer> s : this.knwonServers) {
            sendRequest(request, s);
        }
    }

    public void sendRequest(Request request, List<Integer> server) throws IOException {
        // Send client requests
        byte[] buf = objectToByte(request);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(SERVER_HOST), server.get(1));
        udpSocket.send(packet);
        System.out.println("Request " + request.getType() + " has been broadcasted to all ...");
    }

}
