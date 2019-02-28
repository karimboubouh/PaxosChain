package protocol;

import core.Paxos;
import core.Transaction;

public class Protocol {

    // global request states
    public static final int GET_BALANCE = 1;
    public static final int SEND_BALANCE = 2;
    public static final int ADD_TRANSACTION = 3;
    public static final int SEND_TRANSACTION_STATUS = 4;
    public static final int PREPARE = 5;
    public static final int ACK = 6;
    public static final int PROPOSE = 7;
    public static final int ACCEPT = 8;
    public static final int DECIDE = 9;

    // global error states
    public static final int SEND_BALANCE_ERROR = 101;
    public static final int SEND_TRANSACTION_STATUS_ERROR = 102;

    // ------------------------------ Request messages ------------------------

    /*
        get client balance
    */
    public static Request getBalance(int clientId) {
        return new Request(clientId, GET_BALANCE);
    }

    /*
        execute a transaction
    */
    public static Request addTransaction(int client_id, Transaction transaction) {
        return new Request(client_id, ADD_TRANSACTION, transaction);
    }

    // ------------------------------ Response messages -----------------------

    /*
    get client balance
    */
    public static Response sendBalance(int requestID, int senderID, String message) {

        return new Response(SEND_BALANCE, requestID, senderID, message);
    }

    // ------------------------------ Paxos Request messages ------------------
    public static Request sendPrepare(int senderID, Paxos paxos) {
        return new Request(senderID, Protocol.PREPARE, paxos);
    }

    public static Request sendPropose(int senderID, Paxos paxos) {
        return new Request(senderID, Protocol.PROPOSE, paxos);
    }

    public static Request sendDecide(int senderID, Paxos paxos) {
        return new Request(senderID, Protocol.DECIDE, paxos);
    }

    // ------------------------------ Paxos Response messages ------------------
    public static Response sendAck(int requestID, int senderID, Paxos paxos) {
        return new Response(ACK, requestID, senderID, paxos);
    }

    public static Response sendAccept(int requestID, int senderID, Paxos paxos) {
        return new Response(PROPOSE, requestID, senderID, paxos);
    }
}

