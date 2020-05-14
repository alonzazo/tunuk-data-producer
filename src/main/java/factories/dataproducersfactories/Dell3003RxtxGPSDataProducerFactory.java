package factories.dataproducersfactories;

import producers.DataProducer;
import producers.GPSDataProducer.Dell3003RxtxGPSDataProducer;
import eventbuses.EventBus;

import java.util.Properties;

public class Dell3003RxtxGPSDataProducerFactory implements DataProducerFactory {

    @Override
    public DataProducer create(String configurationId, Properties configurations, EventBus eventBus) throws DataProducerPropertyNotDefinedException {
        String serialPort;

        if (configurations.containsKey(String.format("%s.producer.serialport", configurationId)))
            serialPort = configurations.getProperty(String.format("%s.producer.serialport", configurationId));
        else
            throw new DataProducerPropertyNotDefinedException(String.format("%s.producer.serialport", configurationId));

        return new Dell3003RxtxGPSDataProducer(serialPort, eventBus);
    }

    @Override
    public DataProducer create(Properties configurations, EventBus EventBus) throws DataProducerPropertyNotDefinedException {

        String serialPort;

        if (configurations.containsKey("producer.serialport"))
            serialPort = configurations.getProperty("producer.serialport");
        else
            throw new DataProducerPropertyNotDefinedException("producer.serialport");

        return new Dell3003RxtxGPSDataProducer(serialPort, EventBus);
    }

    @Override
    public DataProducer create(EventBus EventBus) {
        return new Dell3003RxtxGPSDataProducer("/dev/ttyHS0", EventBus);
    }

}
