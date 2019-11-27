package faulttolerance;

import com.sleepycat.je.*;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;


public class BerkleyDBPersistentQueue implements PersistentQueue {

    private final Environment dbEnv;

    private final Database queueDatabase;

    private final int cacheSize;

    private final String queueName;

    private int opsCounter;

    public BerkleyDBPersistentQueue(final String queueEnvPath,
                                    final String queueName,
                                    final int cacheSize) throws PersistentQueueException {

        try {
            new File(queueEnvPath).mkdirs();

            final EnvironmentConfig dbEnvConfig = new EnvironmentConfig();
            dbEnvConfig.setTransactional(false);
            dbEnvConfig.setAllowCreate(true);
            this.dbEnv = new Environment(new File(queueEnvPath),
                    dbEnvConfig);

            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setTransactional(false);
            dbConfig.setAllowCreate(true);
            dbConfig.setDeferredWrite(true);
            dbConfig.setBtreeComparator(new KeyComparator());
            this.queueDatabase = dbEnv.openDatabase(null,
                    queueName,
                    dbConfig);
            this.queueName = queueName;
            this.cacheSize = cacheSize;
            this.opsCounter = 0;
        } catch (Exception e){
            throw new PersistentQueueException(e);
        }

    }
    @Override
    public String pollMessage() throws PersistentQueueException {

        try {
            final DatabaseEntry key = new DatabaseEntry();
            final DatabaseEntry data = new DatabaseEntry();

            final Cursor cursor = queueDatabase.openCursor(null,null);
            try {
                cursor.getFirst(key, data, LockMode.RMW);
                if (data.getData() == null)
                    return null;
                final String res = new String(data.getData(), StandardCharsets.UTF_8);
                cursor.delete();
                opsCounter++;
                if (opsCounter >= cacheSize){
                    queueDatabase.sync();
                    opsCounter = 0;
                }
                return res;
            } finally {
                cursor.close();
            }
        } catch (Exception e){
            throw new PersistentQueueException(e);
        }


    }

    @Override
    public String pickMessage() {
        return null;
    }

    @Override
    public void pushMessage(String message) throws PersistentQueueException {
        try {
            DatabaseEntry key = new DatabaseEntry();
            DatabaseEntry data = new DatabaseEntry();
            Cursor cursor = queueDatabase.openCursor(null, null);
            try {

                cursor.getLast(key, data, LockMode.RMW);

                BigInteger prevKeyValue;
                if (key.getData() == null) {
                    prevKeyValue = BigInteger.valueOf(-1);
                } else {
                    prevKeyValue = new BigInteger(key.getData());
                }
                BigInteger newKeyValue = prevKeyValue.add(BigInteger.ONE);

                try {
                    final DatabaseEntry newKey = new DatabaseEntry(
                            newKeyValue.toByteArray());
                    final DatabaseEntry newData = new DatabaseEntry(
                            message.getBytes("UTF-8"));
                    queueDatabase.put(null, newKey, newData);

                    opsCounter++;
                    if (opsCounter >= cacheSize) {
                        queueDatabase.sync();
                        opsCounter = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                cursor.close();
            }
        } catch (Exception e){
            throw new PersistentQueueException(e);
        }


    }

    @Override
    public void flush() {

    }

    @Override
    public void fetch() {

    }

    /**
     * Returns the size of this queue.
     *
     * @return the size of the queue
     */
    public long size() {
        try {
            return queueDatabase.count();
        }catch (Exception e){
            return 0;
        }

    }

    /**
     * Returns this queue name.
     *
     * @return this queue name
     */
    public String getQueueName(){
        return queueName;
    }

    /**
     * Closes this queue and frees up all resources associated to it.
     */
    public void close(){
        try {
            queueDatabase.close();
            dbEnv.close();
        }catch (Exception ex){
            new PersistentQueueException(ex);
        }
    }

}
