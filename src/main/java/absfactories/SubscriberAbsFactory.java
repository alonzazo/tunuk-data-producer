package absfactories;

import factories.subscribersfactories.EagerIoTDataBusPublisherFactory;
import factories.subscribersfactories.IoTDataBusPublisherFactory;
import factories.subscribersfactories.SubscriberFactory;

public class SubscriberAbsFactory {

    private SubscriberAbsFactory(){}

    public static SubscriberFactory createFactory(SubscriberType type) throws SubscriberNotFoundException
    {
        switch (type){
            case IOT_DATA_BUS_PUBLISHER:
                return new IoTDataBusPublisherFactory();
            case EAGER_IOT_DATA_BUS_PUBLISHER:
                return new EagerIoTDataBusPublisherFactory();
            default:
                throw new SubscriberNotFoundException();
        }
    }
}