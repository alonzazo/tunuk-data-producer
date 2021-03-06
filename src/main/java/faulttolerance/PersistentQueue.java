package faulttolerance;

public interface PersistentQueue {

    /**
     * Get the first message and delete it from the queue
     * @return message
     */
    String pollMessage() throws PersistentQueueException;

    /**
     * Get the first message from the queue
     * @return message
     */
    String peekMessage() throws PersistentQueueException;

    /**
     * Push the message to the queue
     * @param message
     */
    void pushMessage(String message) throws PersistentQueueException;

    long size();

    void close() throws PersistentQueueException;

}
