package protocol;

import core.Host;
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

    // ------------------------------ Client messages ------------------------

    public static Request getBalance(Host sender) {
        return new Request(sender, GET_BALANCE);
    }

    public static Request addTransaction(Host sender, Transaction transaction) {
        return new Request(sender, ADD_TRANSACTION, transaction);
    }

    // ------------------------------ Server messages -------------------------

    public static Response sendBalance(int requestId, Host sender, String message) {
        return new Response(SEND_BALANCE, requestId, sender, message);
    }

    // ------------------------------ Paxos Request messages ------------------

    public static Request sendPrepare(Host sender, Paxos paxos) {
        return new Request(sender, Protocol.PREPARE, paxos);
    }

    public static Request sendPropose(Host sender, Paxos paxos) {
        return new Request(sender, Protocol.PROPOSE, paxos);
    }

    public static Request sendDecide(Host sender, Paxos paxos) {
        return new Request(sender, Protocol.DECIDE, paxos);
    }

    // ------------------------------ Paxos Response messages ------------------

    public static Response sendAck(int requestId, Host sender, Paxos paxos) {
        return new Response(ACK, requestId, sender, paxos);
    }

    public static Response sendAccept(int requestId, Host sender, Paxos paxos) {
        return new Response(ACCEPT, requestId, sender, paxos);
    }
}

