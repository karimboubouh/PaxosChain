package protocol;

import core.Host;
import core.Paxos;
import core.Transaction;

import java.io.Serializable;

import static java.lang.System.exit;

public class Response implements Serializable {
    private static int id = 0;
    private int requestId;
    private int type;
    private int status;
    private Host sender;
    private String message;
    private Transaction transaction = null;
    private Paxos paxos = null;

    // send balance
    public Response(int type, int requestId, Host sender, String message) {
        this.id = ++id;
        this.requestId = requestId;
        this.type = type;
        this.sender = sender;
        this.message = message;
    }

    // Paxos message
    public Response(int type, int requestId, Host sender, Paxos paxos) {
        this.id = ++id;
        this.requestId = requestId;
        this.sender = sender;
        this.type = type;
        switch (type) {
            case Protocol.ACK:
                this.paxos = paxos.ackMessage();
                break;
            case Protocol.ACCEPT:
                this.paxos = paxos.acceptMessage();
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

    public Host getSender() { return sender; }

    public void setSender(Host sender) { this.sender = sender; }

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
