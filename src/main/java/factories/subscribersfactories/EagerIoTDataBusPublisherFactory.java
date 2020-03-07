package factories.subscribersfactories;

import connectors.IoTConnector;
import faulttolerance.PersistentQueue;
import subscribers.EagerIoTDataBusPublisher;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class EagerIoTDataBusPublisherFactory implements SubscriberFactory {

    private IoTConnector ioTConnector;
    private String topic;
    private Function<List<Map<String, String>>, String> handlerFunction;
    private PersistentQueue persistentQueue;

    public IoTConnector getIoTConnector() {
        return ioTConnector;
    }

    public EagerIoTDataBusPublisherFactory setIoTConnector(IoTConnector ioTConnector) {
        this.ioTConnector = ioTConnector; return this;
    }

    public String getTopic() {
        return topic;
    }

    public EagerIoTDataBusPublisherFactory setTopic(String topic) {
        this.topic = topic; return this;
    }

    public Function<List<Map<String, String>>, String> getHandlerFunction() {
        return handlerFunction;
    }

    public EagerIoTDataBusPublisherFactory setHandlerFunction(Function<List<Map<String, String>>, String> handlerFunction) {
        this.handlerFunction = handlerFunction; return this;
    }

    public PersistentQueue getPersistentQueue() {
        return persistentQueue;
    }

    public EagerIoTDataBusPublisherFactory setPersistentQueue(PersistentQueue persistentQueue) {
        this.persistentQueue = persistentQueue; return this;
    }

    @Override
    public EagerIoTDataBusPublisher create() {
        return new EagerIoTDataBusPublisher(ioTConnector,topic,handlerFunction,persistentQueue);
    }
}
