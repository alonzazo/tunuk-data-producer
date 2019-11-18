package absfactories;

import factories.EventBusFactory;
import factories.EventBusType;
import factories.MicrobatchDataBusFactory;
import factories.StreamingEventBusFactory;

public class EventBusAbsFactory {

    public static EventBusFactory create(EventBusType type) throws EventBusNotDefinedException {
        switch (type){
            case MICROBATCH:
                return (EventBusFactory) new MicrobatchDataBusFactory();
            case STREAMING:
                return (EventBusFactory) new StreamingEventBusFactory();
            default:
                throw new EventBusNotDefinedException();
        }
    }
}
