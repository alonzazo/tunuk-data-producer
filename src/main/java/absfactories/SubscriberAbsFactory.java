package absfactories;

import factories.subscribersfactories.IoTDataBusPublisherFactory;
import factories.subscribersfactories.SubscriberFactory;

public class SubscriberAbsFactory {

    private SubscriberAbsFactory(){}

    public static SubscriberFactory createFactory(SubscriberType type) throws SubscriberNotFoundException
    {
        switch (type){
            case IOT_DATA_BUS_PUBLISHER:
                return new IoTDataBusPublisherFactory();
            default:
                throw new SubscriberNotFoundException();
        }
    }
}