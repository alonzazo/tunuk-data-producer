package factories.dataproducersfactories;

import producers.DataProducer;
import producers.GPSDataProducer.HongdianH8922SGPSDataProducer;
import eventbuses.EventBus;

import java.util.Properties;

public class HongdianH8922SGPSDataProducerFactory implements DataProducerFactory{

    @Override
    public DataProducer create(String configurationId, Properties configurations, EventBus eventBus) throws DataProducerPropertyNotDefinedException {
        // Se definen los valores default
        int port;

        // Se valida si existen, si no se queda con valor default
        if (configurations.containsKey(String.format("%s.producer.port", configurationId))){
            port = Integer.parseInt(configurations.getProperty(String.format("%s.producer.port", configurationId)));
        } else {
            throw new DataProducerPropertyNotDefinedException("producer.port");
        }

        return new HongdianH8922SGPSDataProducer(port,eventBus);
    }

    @Override
    public DataProducer create(Properties configurations, EventBus EventBus) throws DataProducerPropertyNotDefinedException {
        // Se definen los valores default
        int port;

        // Se valida si existen, si no se queda con valor default
        if (configurations.containsKey("producer.port")){
            port = Integer.parseInt(configurations.getProperty("producer.port"));
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
