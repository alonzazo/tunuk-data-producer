package factories;

import utils.StreamingEventBus;

public class StreamingEventBusFactory implements EventBusFactory{

    public StreamingEventBus create(){ return new StreamingEventBus(); }
}
