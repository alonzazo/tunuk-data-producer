package eventbuses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import subscribers.Subscriber;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MicrobatchDataBus implements DataBus {

    static Logger log = LoggerFactory.getLogger(MicrobatchDataBus.class);

    private Map<Class<?>, List<Map<String, String>>> dataBus;
    private List<Subscriber> subscribers;
    private long interval;

    public MicrobatchDataBus() {
        dataBus = new ConcurrentHashMap<>();
        subscribers = new LinkedList<>();
        interval = 1000;
    }

    public MicrobatchDataBus(long interval) {
        dataBus = new ConcurrentHashMap<>();
        subscribers = new LinkedList<>();
        this.interval = interval;
    }

    @Override
    public long getInterval() {
        return interval;
    }

    @Override
    public Map<Class<?>, List<Map<String, String>>> getDataBus() {
        return dataBus;
    }

    private List<Map<String, String>> getMergedData() {
        List<Map<String, String>> result = new LinkedList<>();
            for (List<Map<String, String>> data : getDataBus().values())
                result.addAll(data);
        return result;
    }

    @Override
    public void flush() {
        for (List list :
                getDataBus().values()) {
            list.clear();
        }
        getDataBus().clear();
    }

    @Override
    public synchronized void startPublication() {

        Thread thread = new Thread(() -> {

            while (true) {
                try {
                    synchronized (this) {
                        List<Map<String, String>> mergedData = getMergedData();
                        subscribers.forEach(subscriber -> subscriber.handleDataBus(mergedData));
                        flush();
                    }
                    Thread.sleep(getInterval());
                } catch (InterruptedException e) {
                    log.info(e.getMessage());
                }
            }

        });

        thread.setName(String.format("microbatch-%d-ms-thread", getInterval()));

        thread.start();
    }

    @Override
    public synchronized void publishData(Class<?> dataProducer, Map<String, String> data) {
        synchronized (this) {
            List<Map<String, String>> dataBusFromDataDriver = getDataBus().get(dataProducer);
            if (dataBusFromDataDriver != null) {
                dataBusFromDataDriver.add(data);
            } else {
                dataBusFromDataDriver = new LinkedList<>();
                dataBusFromDataDriver.add(data);
                getDataBus().put(dataProducer, dataBusFromDataDriver);
            }
        }
    }

    @Override
    public synchronized void subscribe(Subscriber subscriber) {
        synchronized (this){
            subscribers.add(subscriber);
        }

    }

    @Override
    public synchronized List<Subscriber> getSubscribers() {
        return subscribers;
    }
}
