package factories.dataproducersfactories;

import producers.APCDataProducer.HellaAPCECORS485DataProducer;
import producers.DataProducer;
import eventbuses.EventBus;

import java.util.Properties;

public class HellaAPCECORS485DataProducerFactory implements DataProducerFactory {
    @Override
    public DataProducer create(Properties properties, EventBus eventBus) throws DataProducerPropertyNotDefinedException {
        // Se definen valores default
        int doorId;
        String ipAddress;
        int port;

        // Se valida si existen en las propiedades, si no se deja el valor default
        if (properties.containsKey("producer.doorid"))
            doorId = Integer.parseInt(properties.getProperty("producer.doorid"));
        else
            throw new DataProducerPropertyNotDefinedException("producer.doorid");

        if (properties.containsKey("producer.ipaddress"))
            ipAddress = properties.getProperty("producer.ipaddress");
        else
            throw new DataProducerPropertyNotDefinedException("producer.ipaddress");

        if (properties.containsKey("producer.port"))
            port = Integer.parseInt(properties.getProperty("producer.port"));
        else
            throw new DataProducerPropertyNotDefinedException("producer.port");

        return new HellaAPCECORS485DataProducer(doorId, ipAddress, port, eventBus);
    }

    @Override
    public DataProducer create(EventBus EventBus) {
        return new HellaAPCECORS485DataProducer(1, "10.42.1.221", 10076, EventBus);
    }
}
