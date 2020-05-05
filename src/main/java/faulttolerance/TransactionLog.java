package faulttolerance;



public class TransactionLog {

    private long transactionId;
    private State state;

    public enum State {
        begin, commit, abort
    }
}
