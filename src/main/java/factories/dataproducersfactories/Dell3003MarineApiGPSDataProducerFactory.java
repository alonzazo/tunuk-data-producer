package factories.dataproducersfactories;

import producers.DataProducer;
import producers.GPSDataProducer.Dell3003MarineApiGPSDataProducer;
import eventbuses.EventBus;

import java.util.Properties;

public class Dell3003MarineApiGPSDataProducerFactory implements DataProducerFactory {
    @Override
    public DataProducer create(Properties properties, EventBus EventBus) {
        return new Dell3003MarineApiGPSDataProducer();
    }

    @Override
    public DataProducer create(EventBus EventBus) {
        return new Dell3003MarineApiGPSDataProducer();
    }

}
