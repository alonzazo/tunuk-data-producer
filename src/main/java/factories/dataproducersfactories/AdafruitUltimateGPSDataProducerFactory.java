package factories.dataproducersfactories;

import eventbuses.EventBus;
import producers.DataProducer;
import producers.GPSDataProducer.AdafruitUltimateGPSDataProducer;

import java.util.Properties;

public class AdafruitUltimateGPSDataProducerFactory implements DataProducerFactory{

    @Override
    public DataProducer create(Properties properties, EventBus EventBus) throws DataProducerPropertyNotDefinedException {

        String serialPort;

        if (properties.containsKey("producer.serialport"))
            serialPort = properties.getProperty("producer.serialport");
        else
            throw new DataProducerPropertyNotDefinedException("producer.serialport");

        return new AdafruitUltimateGPSDataProducer(serialPort, EventBus);
    }

    @Override
    public DataProducer create(EventBus EventBus) {
        return new AdafruitUltimateGPSDataProducer("/dev/ttyHS0", EventBus);
    }
}
