package utils;

import consumers.Subscriber;

import java.util.List;
import java.util.Map;

public interface EventBus {

    /**
     *  Publish data to all subscribers
     *
     * @param dataProducer DataProducer queue in which data will be put
     * @param data Data for publishing
     */
    void                            publishData(Class<?> dataProducer, Map<String,String> data);

    /**
     * Add a subscriber which data can be published
     * @param subscriber
     */
    void                            subscribe(Subscriber subscriber);

    /**
     * Get a list of subscribers
     * @return
     */
    List<Subscriber>                getSubscribers();
}
