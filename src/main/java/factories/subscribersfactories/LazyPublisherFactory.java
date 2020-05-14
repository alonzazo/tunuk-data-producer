package factories.subscribersfactories;

import connectors.IoTConnector;
import faulttolerance.PersistentQueue;
import subscribers.LazyPublisher;
import subscribers.Subscriber;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LazyPublisherFactory implements SubscriberFactory {

    private IoTConnector ioTConnector;
    private String topic;
    private Function<List<Map<String, String>>, String> handlerFunction;
    private PersistentQueue persistentQueue;

    public IoTConnector getIoTConnector() {
        return ioTConnector;
    }

    public LazyPublisherFactory setIoTConnector(IoTConnector ioTConnector) {
        this.ioTConnector = ioTConnector; return this;
    }

    public String getTopic() {
        return topic;
    }

    public LazyPublisherFactory setTopic(String topic) {
        this.topic = topic; return this;
    }

    public Function<List<Map<String, String>>, String> getHandlerFunction() {
        return handlerFunction;
    }

    public LazyPublisherFactory setHandlerFunction(Function<List<Map<String, String>>, String> handlerFunction) {
        this.handlerFunction = handlerFunction; return this;
    }

    public PersistentQueue getPersistentQueue() {
        return persistentQueue;
    }

    public LazyPublisherFactory setPersistentQueue(PersistentQueue persistentQueue) {
        this.persistentQueue = persistentQueue; return this;
    }

    @Override
    public LazyPublisher create() {
        return new LazyPublisher(ioTConnector,topic,handlerFunction,persistentQueue);
    }

    @Override
    public Subscriber create(IoTConnector ioTConnector, String topic, Function<List<Map<String, String>>, String> composerFunction, PersistentQueue persistentQueue) {
        return new LazyPublisher(ioTConnector,topic,composerFunction,persistentQueue);
    }
}
