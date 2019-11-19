package factories;

import eventbuses.StreamingEventBus;

public class StreamingEventBusFactory implements EventBusFactory{

    public StreamingEventBus create(){ return new StreamingEventBus(); }
}
