package core;


import java.util.ArrayList;
import java.util.List;

public class Block {

    private  int term;
    private static int counter=0;
    public String previousHashHeader;
    public String transactionHash;
    private List<Transaction> transaction;
    private String nonce;

    public Block(String previousHashHeader, String transactionHash, List<Transaction> transaction, String nonce) {
        counter++;
        term=counter;
        this.previousHashHeader = previousHashHeader;
        this.transactionHash = transactionHash;
        this.transaction = transaction;
        this.nonce=nonce;
    }



    public int getTerm() {
        return term;
    }


    public String getPreviousHashHeader() {
        return previousHashHeader;
    }



    public void setPreviousHashHeader(String previous_hash) {
        this.previousHashHeader = calculateHash(previous_hash);
    }



    public String getTransactionHash() {
        return transactionHash;
    }



    public void setTransactionHash(String transactionHash) {
        this.transactionHash = calculateHash(transactionHash);
    }



    public List<Transaction> getTransaction() {
        return transaction;
    }



    public void setTransaction(List<Transaction> transaction) {
        this.transaction = transaction;
    }

    public void setNonce(String nonce) {
        this.nonce=nonce;
    }
    public String getNonce() {
        return nonce;
    }

    public static  String calculateHash(String data) {
        String calculatedhash = StringUtil.applySha256(
                data
        );
        return calculatedhash;
    }

}
	