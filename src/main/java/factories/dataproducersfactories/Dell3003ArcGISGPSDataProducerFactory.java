package factories.dataproducersfactories;

import producers.DataProducer;
import producers.GPSDataProducer.Dell3003ArcGISGPSDataProducer;
import eventbuses.EventBus;

import java.util.Properties;

public class Dell3003ArcGISGPSDataProducerFactory implements DataProducerFactory {
    @Override
    public DataProducer create(Properties properties, EventBus EventBus) throws DataProducerPropertyNotDefinedException {

        String port;

        if (properties.containsKey("producer.port"))
            port = properties.getProperty("producer.port");
        else
            throw new DataProducerPropertyNotDefinedException("producer.port");
        return new Dell3003ArcGISGPSDataProducer(port);
    }

    @Override
    public DataProducer create(EventBus EventBus) {
        return new Dell3003ArcGISGPSDataProducer("/dev/ttyS0");
    }

}
