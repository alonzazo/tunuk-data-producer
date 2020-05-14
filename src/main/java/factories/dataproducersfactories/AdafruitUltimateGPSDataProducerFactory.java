package factories.dataproducersfactories;

import eventbuses.EventBus;
import producers.DataProducer;
import producers.GPSDataProducer.AdafruitUltimateGPSDataProducer;

import java.util.Properties;

public class AdafruitUltimateGPSDataProducerFactory implements DataProducerFactory{

    @Override
    public DataProducer create(String configurationId, Properties configurations, EventBus eventBus) throws DataProducerPropertyNotDefinedException {
        String serialPort;

        if (configurations.containsKey(String.format("%s.producer.serialport", configurationId)))
            serialPort = configurations.getProperty(String.format("%s.producer.serialport", configurationId));
        else
            throw new DataProducerPropertyNotDefinedException(String.format("%s.producer.serialport", configurationId));

        return new AdafruitUltimateGPSDataProducer(serialPort, eventBus);
    }

    @Override
    public DataProducer create(Properties configurations, EventBus EventBus) throws DataProducerPropertyNotDefinedException {

        String serialPort;

        if (configurations.containsKey("producer.serialport"))
            serialPort = configurations.getProperty("producer.serialport");
        else
            throw new DataProducerPropertyNotDefinedException("producer.serialport");

        return new AdafruitUltimateGPSDataProducer(serialPort, EventBus);
    }

    @Override
    public DataProducer create(EventBus EventBus) {
        return new AdafruitUltimateGPSDataProducer("/dev/ttyHS0", EventBus);
    }
}
