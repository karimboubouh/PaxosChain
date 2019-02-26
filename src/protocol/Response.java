package protocol;

import core.PaxosMessage;
import core.Transaction;

import java.io.Serializable;

public class Response implements Serializable {
    private int id;
    private int type;
    private int status;
    private int error;
    private int masterServer;
    private String message;
    private Transaction transaction = null;
    private PaxosMessage paxosMessage = null;

    public Response(int id, int type, int status, int error, int masterServer, String message, PaxosMessage paxosMessage) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.error = error;
        this.masterServer = masterServer;
        this.message = message;
        this.paxosMessage = paxosMessage;
    }

    public Response(int id, int type, int status, int error, int masterServer, String message, Transaction transaction) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.error = error;
        this.masterServer = masterServer;
        this.message = message;
        this.transaction = transaction;
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

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public int getMasterServer() {
        return masterServer;
    }

    public void setMasterServer(int masterServer) {
        this.masterServer = masterServer;
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
