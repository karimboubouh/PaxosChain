package core;

import java.io.Serializable;
import java.util.Arrays;

public class Paxos implements Serializable {
    private int[] ballotNum;
    private int[] acceptNum;
    private Block acceptVal;
    private Block clientVal;

    public Paxos(int[] ballotNum) {
        this.ballotNum = ballotNum;
    }

    public Paxos(Host server) {
        this.ballotNum = new int[]{0, server.getId()};
        acceptNum = new int[]{0, 0};
        acceptVal = null;
    }

    public Paxos(int[] ballotNum, int[] acceptNum, Block acceptVal, Block clientVal) {
        this.ballotNum = ballotNum;
        this.acceptNum = acceptNum;
        this.acceptVal = acceptVal;
        this.clientVal = clientVal;
    }

    public Paxos prepareMessage() {
        this.ballotNum[0]++;
        return new Paxos(this.ballotNum, null, null, null);
    }

    public Paxos ackMessage() {
        return new Paxos(this.ballotNum, this.acceptNum, this.acceptVal, null);
    }

    public Paxos proposeMessage() {
        return new Paxos(this.ballotNum, null, null, clientVal);
    }

    public Paxos acceptMessage() {
        return new Paxos(this.ballotNum, null, null, clientVal);
    }

    public Paxos decideMessage() {
        return new Paxos(this.ballotNum, null, null, clientVal);
    }

    public boolean checkBallotNumber(int[] b) {
        if (this.ballotNum[0] == b[0]) {
            return this.ballotNum[1] > b[1];
        }
        return this.ballotNum[0] > b[0];
    }

    public boolean compareAcceptNum(int[] a) {
        if (this.acceptNum[0] == a[0]) {
            return this.acceptNum[1] > a[1];
        }
        return this.acceptNum[0] > a[0];
    }

    public Block agreeOnBlock(Block currentBlock) {
        return null;
    }

    public int[] getBallotNum() {
        return ballotNum;
    }

    public void setBallotNum(int[] ballotNum) {
        this.ballotNum = ballotNum;
    }

    public int[] getAcceptNum() {
        return acceptNum;
    }

    public void setAcceptNum(int[] acceptNum) {
        this.acceptNum = acceptNum;
    }

    public Block getAcceptVal() {
        return acceptVal;
    }

    public void setAcceptVal(Block acceptVal) {
        this.acceptVal = acceptVal;
    }

    public Block getClientVal() {
        return clientVal;
    }

    public void setClientVal(Block clientVal) {
        this.clientVal = clientVal;
    }

    @Override
    public String toString() {
        return "Paxos{" + Arrays.toString(ballotNum) + '}';
    }
}
