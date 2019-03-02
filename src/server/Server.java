package server;

import client.Client;
import core.*;
import protocol.Protocol;
import protocol.Request;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    // server identity
    private Host host;
    // Communication socket
    private List<Host> servers;
    private DatagramSocket udpSocket;
    private List<ServerHandler> workers;

    // server attributes
    private Host leader = null;
    private int majority;
    private BlockChain BlockChain;
    private Paxos paxos;
    private List<Transaction> transactions;
    private Map<Integer, Float> balances = new HashMap<>();

    // Paxos attributes
    private ConcurrentHashMap<int[], List<Paxos>> acks;
    private Map<int[], List<Paxos>> accepts;


    public Server(Host host) throws IOException, ClassNotFoundException {
        this.host = host;
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
        this.servers = Global.servers();
        this.majority = this.servers.size() / 2 + 1;
        this.udpSocket = new DatagramSocket(host.getPort(), host.getAddress());
        this.workers = new ArrayList<>();

        // init server info
        this.BlockChain = new BlockChain();
        this.paxos = new Paxos(host);
        this.transactions = new ArrayList<>();
        this.balances = new HashMap<>();

        // init paxos attributes
        this.acks = new ConcurrentHashMap<>();
        this.accepts = new HashMap<>();

        // tmp user balances
        balances.put(1, 100f);
        balances.put(2, 200f);
        balances.put(3, 150f);

    }

    public void start() throws IOException, ClassNotFoundException {
        System.out.println("Server started\nWaiting for requests ...");
        while (true) {
            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            udpSocket.receive(packet);
            Object message = byteToObject(packet.getData());
            ServerHandler serverHandler = new ServerHandler(this, message, packet.getAddress(), packet.getPort());
            serverHandler.start();
            workers.add(serverHandler);
        }
    }

    public Float getUserBalance(Host user) {
        return balances.get(user.getId());
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

    /*
     * Paxos receive ACK : receive ack messages and add them to a buffer.
     * Once you have a majority of acks check for the val of the highest
     * ballot number and retrive its value or use client value.
     * Then send Accept message to all
     */
    public void newAck(Paxos receivedPaxos) throws IOException {
//        int[] b = receivedPaxos.getBallotNum();
        int[] b = this.paxos.getBallotNum();
        Block val = this.paxos.getClientVal();
        if (acks.containsKey(b)) {
            acks.get(b).add(receivedPaxos);
        } else {
            List<Paxos> p = new ArrayList<>();
            p.add(receivedPaxos);
            acks.put(b, p);
        }
        System.out.println("i'm here ...");
        System.out.println(acks);
        System.out.println("> " + acks.size() + " ACK : " + acks.get(b).size() + "(" + b[0] + ", " + b[1] + ") | Majority : " + majority);


        if (acks.get(b).size() >= majority) {
            System.out.println("MASAKAAA");
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
            Request request = Protocol.sendPropose(host, this.paxos);
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
        if (accepts.get(b).size() >= majority) {
            Request request = Protocol.sendDecide(host, this.paxos);
            broadcastRequest(request);
        }
    }


    public void broadcastRequest(Request request) throws IOException {
        System.out.println("Broadcast Request " + request.getType() + " to all ...");
        for (Host s : this.servers) {
            sendRequest(request, s);
        }
    }

    public void sendRequest(Request request, Host server) throws IOException {
        // Send client requests
        byte[] buf = objectToByte(request);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, server.getAddress(), server.getPort());
        udpSocket.send(packet);
        System.out.println("Request " + request.getType() + " has been sent to " + server + " ...");
    }


    public Host getLeader() {
        return leader;
    }

    public void setLeader(Host leader) {
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

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public BlockChain getBlockChain() {
        return BlockChain;
    }

    public void setBlockChain(BlockChain BlockChain) {
        this.BlockChain = BlockChain;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        int id = Integer.parseInt(args[0]);
        new Server(Global.servers().get(id-1));
    }
}
