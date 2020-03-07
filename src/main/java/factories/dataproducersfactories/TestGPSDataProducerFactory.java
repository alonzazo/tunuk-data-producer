package factories.dataproducersfactories;

import eventbuses.EventBus;
import producers.DataProducer;
import producers.GPSDataProducer.TestGPSDataProducer;

import java.util.Properties;

public class TestGPSDataProducerFactory implements DataProducerFactory{
    @Override
    public DataProducer create(Properties properties, EventBus EventBus) throws DataProducerPropertyNotDefinedException {
        return create(EventBus);
    }

    @Override
    public DataProducer create(EventBus EventBus) {
        return new TestGPSDataProducer(EventBus);
    }
}
