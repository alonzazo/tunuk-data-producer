package factories.dataproducersfactories;

import producers.DataProducer;
import producers.GPSDataProducer.Dell3003RxtxGPSDataProducer;
import utils.DataBus;

import java.util.Properties;

public class Dell3003RxtxGPSDataProducerFactory implements DataProducerFactory {
    @Override
    public DataProducer create(Properties properties, DataBus dataBus) throws DataProducerPropertyNotDefinedException {

        String serialPort;

        if (properties.containsKey("producer.serialport"))
            serialPort = properties.getProperty("producer.serialport");
        else
            throw new DataProducerPropertyNotDefinedException("producer.serialport");

        return new Dell3003RxtxGPSDataProducer(serialPort, dataBus);
    }

    @Override
    public DataProducer create(DataBus dataBus) {
        return new Dell3003RxtxGPSDataProducer("/dev/ttyHS0", dataBus);
    }

}
