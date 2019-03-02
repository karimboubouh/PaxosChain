package protocol;

import core.Host;
import core.Paxos;
import core.Transaction;

import java.io.Serializable;

import static java.lang.System.exit;

public class Request implements Serializable {
    private static int id = 0;
    private Host sender;
    private int type;
    private String message;
    private Transaction transaction = null;
    private Paxos paxos = null;


    // getBalance
    public Request(Host sender, int type) {
        this.id = ++id;
        this.sender = sender;
        this.type = type;
    }

    // addTransaction
    public Request(Host sender, int type, Transaction transaction) {
        this.id = ++id;
        this.sender = sender;
        this.type = type;
        this.transaction = transaction;
    }


    // Paxos message
    public Request(Host sender, int type, Paxos paxos) {
        this.id = ++id;
        this.sender = sender;
        this.type = type;
        switch (type) {
            case Protocol.PREPARE:
                this.paxos = paxos.prepareMessage();
                break;
            case Protocol.PROPOSE:
                this.paxos.proposeMessage();
                break;
            case Protocol.DECIDE:
                this.paxos.decideMessage();
                break;
            default:
                System.out.println("Wrong Paxos message !");
                exit(0);
        }

    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public Host getSender() { return sender; }

    public void setSender(Host sender) { this.sender = sender; }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
