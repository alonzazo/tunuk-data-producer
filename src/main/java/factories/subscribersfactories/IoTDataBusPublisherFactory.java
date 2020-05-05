package factories.subscribersfactories;

import connectors.IoTConnector;
import faulttolerance.PersistentQueue;
import subscribers.IoTDataBusPublisher;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class IoTDataBusPublisherFactory implements SubscriberFactory {

    private IoTConnector ioTConnector;
    private String topic;
    private Function<List<Map<String, String>>, String> handlerFunction;
    private PersistentQueue persistentQueue;

    public IoTConnector getIoTConnector() {
        return ioTConnector;
    }

    public IoTDataBusPublisherFactory setIoTConnector(IoTConnector ioTConnector) {
        this.ioTConnector = ioTConnector; return this;
    }

    public String getTopic() {
        return topic;
    }

    public IoTDataBusPublisherFactory setTopic(String topic) {
        this.topic = topic; return this;
    }

    public Function<List<Map<String, String>>, String> getHandlerFunction() {
        return handlerFunction;
    }

    public IoTDataBusPublisherFactory setHandlerFunction(Function<List<Map<String, String>>, String> handlerFunction) {
        this.handlerFunction = handlerFunction; return this;
    }

    public PersistentQueue getPersistentQueue() {
        return persistentQueue;
    }

    public IoTDataBusPublisherFactory setPersistentQueue(PersistentQueue persistentQueue) {
        this.persistentQueue = persistentQueue; return this;
    }

    @Override
    public IoTDataBusPublisher create() {
        return new IoTDataBusPublisher(ioTConnector,topic,handlerFunction,persistentQueue);
    }
}
