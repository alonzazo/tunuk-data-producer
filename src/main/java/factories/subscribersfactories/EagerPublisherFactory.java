package factories.subscribersfactories;

import connectors.IoTConnector;
import faulttolerance.PersistentQueue;
import subscribers.EagerPublisher;
import subscribers.Subscriber;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class EagerPublisherFactory implements SubscriberFactory {

    private IoTConnector ioTConnector;
    private String topic;
    private Function<List<Map<String, String>>, String> handlerFunction;
    private PersistentQueue persistentQueue;

    public IoTConnector getIoTConnector() {
        return ioTConnector;
    }

    public EagerPublisherFactory setIoTConnector(IoTConnector ioTConnector) {
        this.ioTConnector = ioTConnector; return this;
    }

    public String getTopic() {
        return topic;
    }

    public EagerPublisherFactory setTopic(String topic) {
        this.topic = topic; return this;
    }

    public Function<List<Map<String, String>>, String> getHandlerFunction() {
        return handlerFunction;
    }

    public EagerPublisherFactory setHandlerFunction(Function<List<Map<String, String>>, String> handlerFunction) {
        this.handlerFunction = handlerFunction; return this;
    }

    public PersistentQueue getPersistentQueue() {
        return persistentQueue;
    }

    public EagerPublisherFactory setPersistentQueue(PersistentQueue persistentQueue) {
        this.persistentQueue = persistentQueue; return this;
    }

    @Override
    public EagerPublisher create() {
        return new EagerPublisher(ioTConnector,topic,handlerFunction,persistentQueue);
    }

    @Override
    public Subscriber create(IoTConnector ioTConnector, String topic, Function<List<Map<String, String>>, String> composerFunction, PersistentQueue persistentQueue) {
        return new EagerPublisher(ioTConnector,topic,composerFunction,persistentQueue);
    }
}
