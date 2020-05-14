package absfactories;

import factories.subscribersfactories.EagerPublisherFactory;
import factories.subscribersfactories.LazyPublisherFactory;
import factories.subscribersfactories.SubscriberFactory;

public class SubscriberAbsFactory {

    private SubscriberAbsFactory(){}



    public static SubscriberFactory createFactory(SubscriberType type) throws SubscriberNotFoundException
    {
        switch (type){
            case LAZY_PUBLISHER:
                return new LazyPublisherFactory();
            case EAGER_PUBLISHER:
                return new EagerPublisherFactory();
            default:
                throw new SubscriberNotFoundException();
        }
    }
}