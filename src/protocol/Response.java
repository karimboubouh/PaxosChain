package protocol;

import core.Paxos;
import core.Transaction;

import java.io.Serializable;

import static java.lang.System.exit;

public class Response implements Serializable {
    private static int id = 0;
    private int requestID;
    private int type;
    private int status;
    private int senderID;
    private String message;
    private Transaction transaction = null;
    private Paxos paxos = null;

    // send balance
    public Response(int type, int requestID, int senderID, String message) {
        this.id = ++id;
        this.requestID = requestID;
        this.type = type;
        this.senderID = senderID;
        this.message = message;
    }

    public Response(int id, int type, int status, int senderID, String message, Paxos paxos) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.senderID = senderID;
        this.message = message;
        this.paxos = paxos;
    }

    public Response(int id, int type, int status, int senderID, String message, Transaction transaction) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.senderID = senderID;
        this.message = message;
        this.transaction = transaction;
    }

    // Paxos message
    public Response(int senderID, int requestID, int type, Paxos paxos) {
        this.id = ++id;
        this.requestID = requestID;
        this.senderID = senderID;
        this.type = type;
        switch (type) {
            case Protocol.ACK:
                this.paxos.ackMessage();
                break;
            case Protocol.ACCEPT:
                this.paxos.acceptMessage();
                break;
            default:
                System.out.println("Wrong Paxos message !");
                exit(0);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getsenderID() {
        return senderID;
    }

    public void setsenderID(int senderID) {
        this.senderID = senderID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Paxos getPaxos() {
        return paxos;
    }

    public void setPaxos(Paxos paxos) {
        this.paxos = paxos;
    }
}
