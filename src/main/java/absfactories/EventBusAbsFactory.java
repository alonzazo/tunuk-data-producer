package absfactories;

import factories.eventbusesfactories.EventBusFactory;
import factories.eventbusesfactories.EventBusType;
import factories.eventbusesfactories.MicrobatchDataBusFactory;
import factories.eventbusesfactories.StreamingEventBusFactory;

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
