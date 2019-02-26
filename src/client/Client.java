package client;

import core.Transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    //
    public static final String serverHost = "localhost";
    public static final int PORT = 9065;
    public static final int CLIENT_ID = 1;
    //
    private Socket socket = null;
    private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;
    //
    private List<Transaction> transactions;


    public Client() throws IOException {
        init();
        start();
    }

    private void init() throws IOException {
        // init client info
        transactions = new ArrayList<>();
        // init client socket
        this.socket = new Socket(InetAddress.getByName(serverHost), PORT);
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
    }

    private void start() {
        // start a transaction request ...
    }

    @Override
    public String toString() {
        return "Client{"+ CLIENT_ID +"}";
    }

    public static void main(String[] args) throws IOException {
        new Client().start();
    }
}
