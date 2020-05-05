package faulttolerance;

public class PersistentQueueException extends Exception {
    public PersistentQueueException() {
        super();
    }

    public PersistentQueueException(String message) {
        super(message);
    }

    public PersistentQueueException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistentQueueException(Throwable cause) {
        super(cause);
    }

    protected PersistentQueueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
