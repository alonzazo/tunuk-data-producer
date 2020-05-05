package factories.persistentqueuesfactories;

import faulttolerance.BerkleyDBPersistentQueue;
import faulttolerance.PersistentQueue;

public class BerkleyDBPersistentQueueFactory implements PersistentQueueFactory {

    private String queueEnvironmentPath = "/tmp";
    private String queueName = "berkleydb";
    private int cacheSize = 10;

    public String getQueueEnvironmentPath() {
        return queueEnvironmentPath;
    }

    public BerkleyDBPersistentQueueFactory setQueueEnvironmentPath(String queueEnvironmentPath) {
        this.queueEnvironmentPath = queueEnvironmentPath; return this;
    }

    public String getQueueName() {
        return queueName;
    }

    public BerkleyDBPersistentQueueFactory setQueueName(String queueName) {
        this.queueName = queueName; return this;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public BerkleyDBPersistentQueueFactory setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize; return this;
    }

    @Override
    public PersistentQueue create() {
        try {
            return new BerkleyDBPersistentQueue(queueEnvironmentPath,queueName,cacheSize);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
}
