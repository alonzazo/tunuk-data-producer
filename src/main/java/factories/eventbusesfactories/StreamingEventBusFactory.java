package factories.eventbusesfactories;

import eventbuses.StreamingEventBus;

public class StreamingEventBusFactory implements EventBusFactory{

    public StreamingEventBus create(){ return new StreamingEventBus(); }
}
