package protocol;

import core.PaxosMessage;
import core.Transaction;

import java.io.Serializable;

public class Request implements Serializable {
    private int id;
    private int type;
    private String message;
    private Transaction transaction = null;
    private PaxosMessage paxosMessage = null;

    public Request(int id, int type, String message, Transaction transaction) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.transaction = transaction;
    }

    public Request(int id, int type, String message, PaxosMessage paxosMessage) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.paxosMessage = paxosMessage;
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

    public PaxosMessage getPaxosMessage() {
        return paxosMessage;
    }

    public void setPaxosMessage(PaxosMessage paxosMessage) {
        this.paxosMessage = paxosMessage;
    }
}
