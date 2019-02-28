package protocol;

import core.Paxos;
import core.Transaction;

import java.io.Serializable;

import static java.lang.System.exit;

public class Request implements Serializable {
    private static int id = 0;
    private int sender;
    private int type;
    private String message;
    private Transaction transaction = null;
    private Paxos paxos = null;


    // getBalance
    public Request(int client_id, int type) {
        this.id = ++id;
        this.sender = client_id;
        this.type = type;
    }

    // addTransaction
    public Request(int client_id, int type, Transaction transaction) {
        this.id = ++id;
        this.sender = client_id;
        this.type = type;
        this.transaction = transaction;
    }


    // Paxos message
    public Request(int serverID, int type, Paxos paxos) {
        this.id = ++id;
        this.sender = serverID;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

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
