package core;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockChain {

    List<Block> blockChain;

    public BlockChain() {
        this.blockChain = new ArrayList<>();
    }


    private static String nonce(String transaction) {

        String hash;
        String nonce;
        do {
            nonce = getSaltString();
            hash = Block.calculateHash(transaction + nonce);
//            System.out.println(hash);
        } while (!hash.endsWith("0") && !hash.endsWith("1") && !hash.endsWith("2"));
        return nonce;
    }

    public static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 26) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    public static String getHeaer(List<Block> blockckain, int index) {

        int term = blockckain.get(index).getTerm();
        String previousHashHeader = blockckain.get(index).getPreviousHashHeader();
        String transactionHash = blockckain.get(index).getTransactionHash();
        String nonce = blockckain.get(index).getNonce();

        return term + previousHashHeader + transactionHash + nonce;

    }

    public static String getPreviousHeaer(List<Block> blockckain) {
        if(blockckain.size() > 0){
            int term = blockckain.get(blockckain.size() - 1).getTerm();
            String previousHashHeader = blockckain.get(blockckain.size() - 1).getPreviousHashHeader();
            String transactionHash = blockckain.get(blockckain.size() - 1).getTransactionHash();
            String nonce = blockckain.get(blockckain.size() - 1).getNonce();

            return term + previousHashHeader + transactionHash + nonce;
        }else {
            return "0";
        }

    }

    public static String checkBlockchain(List<Block> blockckain) {
        String prHash;
        String realHash;
        for (int i = blockckain.size() - 1; i > 0; i--) {
            prHash = blockckain.get(i).getPreviousHashHeader();
            realHash = Block.calculateHash(getHeaer(blockckain, i - 1));
            if (!prHash.equals(realHash)) {
                return "compatible";
            }
        }
        return "incompatible!";
    }

    public void addBlock(Block b){
        this.blockChain.add(b);
    }

    public Block newBlock(List<Transaction> transactions){
        return new Block(getPreviousHeaer(this.blockChain),
                Block.calculateHash(transactions.get(0).toString() + transactions.get(1).toString()),
                transactions,
                nonce(Block.calculateHash(transactions.get(0).toString() + transactions.get(1).toString()))
        );
    }

//    public static void main(String[] args) {
//        // TODO Auto-generated method stub
//        ArrayList<Block> blockchain = new ArrayList<Block>();
//        ArrayList<String> trns = new ArrayList<>();
//        ArrayList<String> trns2 = new ArrayList<>();
//        trns.add("A B 100");
//        trns.add("C A 300");
//        trns2.add("B A 100");
//        trns2.add("A C 400");
//        System.out.println("starting........");
////        blockchain.add(new Block(null, Block.calculateHash(trns.get(0) + trns.get(1)), trns, nonce(Block.calculateHash(trns.get(0) + trns.get(1)))));
////        blockchain.add(new Block(getPreviousHeaer(blockchain), Block.calculateHash(trns2.get(0) + trns2.get(1)), trns2, nonce(Block.calculateHash(trns2.get(0) + trns2.get(1)))));
//        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
//        System.out.println(blockchainJson);
//        System.out.println("the blockchain is: " + checkBlockchain(blockchain));
//
//    }


}