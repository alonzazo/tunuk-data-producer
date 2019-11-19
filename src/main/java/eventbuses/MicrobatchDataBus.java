package eventbuses;

import subscribers.Subscriber;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MicrobatchDataBus implements DataBus {

    private Map<Class<?>, List<Map<String,String>>> dataBus;
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
    public synchronized long getInterval() {
        return interval;
    }

    @Override
    public synchronized Map<Class<?>, List<Map<String, String>>> getDataBus() {
        return dataBus;
    }

    private List<Map<String, String>> getMergedData() {
        List<Map<String,String>> result = new LinkedList<>();
        for (List<Map<String,String>> data: dataBus.values())
            result.addAll(data);
        return result;
    }

    @Override
    public synchronized void flush() {
        for (List list:
                dataBus.values()) {
            list.clear();
        }
        dataBus.clear();
    }

    @Override
    public synchronized void startPublication() {
        Thread thread = new Thread(() -> {

            while (true){
                try {
                    subscribers.forEach(subscriber -> subscriber.handleDataBus(getMergedData()));
                    flush();
                    Thread.sleep(getInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

        thread.start();
    }

    @Override
    public synchronized void publishData(Class<?> dataProducer, Map<String, String> data) {
        List<Map<String, String>> dataBusFromDataDriver = dataBus.get(dataProducer);
        if (dataBusFromDataDriver != null){
            dataBusFromDataDriver.add(data);
        } else {
            dataBusFromDataDriver = new LinkedList<>();
            dataBusFromDataDriver.add(data);
            dataBus.put(dataProducer,dataBusFromDataDriver);
        }
    }

    @Override
    public synchronized void subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public synchronized List<Subscriber> getSubscribers() {
        return subscribers;
    }
}
