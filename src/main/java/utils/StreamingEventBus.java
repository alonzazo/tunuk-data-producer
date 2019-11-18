package utils;

import consumers.Subscriber;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StreamingEventBus implements EventBus{

    private List<Subscriber> subscribers;

    public StreamingEventBus() {
        subscribers = new LinkedList<>();
    }

    @Override
    public List<Subscriber> getSubscribers() {
        return subscribers;
    }

    @Override
    public void publishData(Class<?> dataProducer, Map<String, String> data) {

        List<Map<String,String>> dataList = new LinkedList<>();
        dataList.add(data);
        for (Subscriber subscriber :
                subscribers) {
            subscriber.handleDataBus(dataList);
        }

    }

    @Override
    public void subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
    }
}
