package factories.subscribersfactories;

import connectors.IoTConnector;
import faulttolerance.PersistentQueue;
import subscribers.Subscriber;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface SubscriberFactory {
    Subscriber create();
    Subscriber create(IoTConnector ioTConnector, String topic, Function<List<Map<String, String>>, String> composerFunction, PersistentQueue persistentQueue);
}
