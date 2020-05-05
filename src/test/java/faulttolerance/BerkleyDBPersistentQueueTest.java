package faulttolerance;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class BerkleyDBPersistentBerkleyDBPersistentQueueTest {

    @Test
    public void testCreateQueue() {
        File queueDir = TestUtils.createTempSubdir("test-queue");
        try {
            BerkleyDBPersistentQueue queue = new BerkleyDBPersistentQueue(queueDir.getPath(), "test-queue", 3);
            try {
                assert Arrays.asList(queueDir.listFiles()).contains(new File(queueDir, "00000000.jdb"));
            } finally {
                queue.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Test
    void pollMessage() {

    }

    @Test
    void pickMessage() {
    }

    @Test
    void pushMessage() {
        File queueDir = TestUtils.createTempSubdir("test-queue");
        try {
            BerkleyDBPersistentQueue queue = new BerkleyDBPersistentQueue(queueDir.getPath(), "test-queue", 3);
            try {
                queue.pushMessage("1");
                queue.pushMessage("2");
                String head = queue.pollMessage();

                assert head.equals("1");
            } finally {
                queue.close();
            }
        }catch (Exception ignored){
            ignored.printStackTrace();
        }
    }

    @Test
    void size() {
    }

    @Test
    void getQueueName() {
    }

    @Test
    void close() {
    }

    @Test public void testQueueSurviveReopen() throws Throwable {
        File queueDir = TestUtils.createTempSubdir("test-queue");
        BerkleyDBPersistentQueue queue = new BerkleyDBPersistentQueue(queueDir.getPath(), "test-queue", 3);
        try {
            queue.pushMessage("5");
            queue.pushMessage("2");
            queue.pushMessage("3");
            queue.pushMessage("4");
        } finally {
            queue.close();
        }

        queue = new BerkleyDBPersistentQueue(queueDir.getPath(), "test-queue", 3);
        try {
            String head = queue.pollMessage();

            assert head.equals("5");
        } finally {
            queue.close();
        }
    }

    @Test public void testQueuePushOrder() throws Throwable {
        File queueDir = TestUtils.createTempSubdir("test-queue");
        final BerkleyDBPersistentQueue queue = new BerkleyDBPersistentQueue(queueDir.getPath(), "test-queue", 1000);
        try {
            for (int i = 0; i < 300; i++) {
                queue.pushMessage(Integer.toString(i));
            }

            for (int i = 0; i < 300; i++) {
                String element = queue.pollMessage();
                if (!Integer.toString(i).equals(element)) {
                    throw new AssertionError("Expected element " + i + ", but got " + element);
                }
            }
        } finally {
            queue.close();
        }

    }

    @Test public void testMultiThreadedPoll() throws Throwable {
        File queueDir = TestUtils.createTempSubdir("test-queue");
        final BerkleyDBPersistentQueue queue = new BerkleyDBPersistentQueue(queueDir.getPath(), "test-queue", 3);
        try {
            int threadCount = 20;
            for (int i = 0; i < threadCount; i++)
                queue.pushMessage(Integer.toString(i));

            final Set set = Collections.synchronizedSet(new HashSet());
            final CountDownLatch startLatch = new CountDownLatch(threadCount);
            final CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                new Thread() {
                    public void run() {
                        try {
                            startLatch.countDown();
                            startLatch.await();

                            String val = queue.pollMessage();
                            if (val != null) {
                                set.add(val);
                            }
                            latch.countDown();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }

            latch.await(5, TimeUnit.SECONDS);

            assert set.size() == threadCount;
        } finally {
            queue.close();
        }
    }

    @Test public void testMultiThreadedPush() throws Throwable {
        File queueDir = TestUtils.createTempSubdir("test-queue");
        final BerkleyDBPersistentQueue queue = new BerkleyDBPersistentQueue(queueDir.getPath(), "test-queue", 3);
        try {
            int threadCount = 20;

            final CountDownLatch startLatch = new CountDownLatch(threadCount);
            final CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                new Thread(Integer.toString(i)) {
                    public void run() {
                        try {
                            startLatch.countDown();
                            startLatch.await();

                            queue.pushMessage(getName());
                            latch.countDown();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }

            latch.await(5, TimeUnit.SECONDS);

            assert queue.size() == threadCount;
        } finally {
            queue.close();
        }
    }

    public static void main(String[] args) throws Throwable {
        int elementCount = 10000;
        File queueDir = TestUtils.createTempSubdir("test-queue");
        final BerkleyDBPersistentQueue queue = new BerkleyDBPersistentQueue(queueDir.getPath(), "test-queue", 1000);
        try {
            long pushStart = System.currentTimeMillis();
            for (int i = 0; i < elementCount; i++) {
                queue.pushMessage(Integer.toString(i));
            }
            long pushEnd = System.currentTimeMillis();
            System.out.println("Time to push " + elementCount + " records: " + (pushEnd - pushStart) + " ms");

            long pollStart = System.currentTimeMillis();
            for (int i = 0; i < elementCount; i++) {
                String element = queue.pollMessage();
                if (!Integer.toString(i).equals(element)) {
                    throw new AssertionError("Expected element " + i + ", but got " + element);
                }
            }
            long pollEnd = System.currentTimeMillis();
            System.out.println("Time to poll " + elementCount + " records: " + (pollEnd - pollStart) + " ms");
        } finally {
            queue.close();
        }
    }
}