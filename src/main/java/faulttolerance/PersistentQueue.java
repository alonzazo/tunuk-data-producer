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
    String pickMessage();

    /**
     * Push the message to the queue
     * @param message
     */
    void pushMessage(String message) throws PersistentQueueException;

    /**
     * Saves all the queue into disk
     */
    void flush();

    /**
     * Restore the queue from disk
     */
    void fetch();

}
