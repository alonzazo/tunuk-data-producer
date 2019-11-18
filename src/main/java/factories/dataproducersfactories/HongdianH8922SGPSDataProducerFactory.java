package factories.dataproducersfactories;

import producers.DataProducer;
import producers.GPSDataProducer.HongdianH8922SGPSDataProducer;
import utils.EventBus;

import java.util.Properties;

public class HongdianH8922SGPSDataProducerFactory implements DataProducerFactory{

    @Override
    public DataProducer create(Properties properties, EventBus EventBus) throws DataProducerPropertyNotDefinedException {
        // Se definen los valores default
        int port;

        // Se valida si existen, si no se queda con valor default
        if (properties.containsKey("producer.port")){
            port = Integer.parseInt(properties.getProperty("producer.port"));
        } else {
            throw new DataProducerPropertyNotDefinedException("producer.port");
        }

        return new HongdianH8922SGPSDataProducer(port,EventBus);
    }

    @Override
    public DataProducer create(EventBus EventBus) {
        return new HongdianH8922SGPSDataProducer(2502, EventBus);
    }
}
