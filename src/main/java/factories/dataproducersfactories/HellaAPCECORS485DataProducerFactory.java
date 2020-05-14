package factories.dataproducersfactories;

import producers.APCDataProducer.HellaAPCECORS485DataProducer;
import producers.DataProducer;
import eventbuses.EventBus;

import java.util.Properties;

public class HellaAPCECORS485DataProducerFactory implements DataProducerFactory {

    @Override
    public DataProducer create(String configurationId, Properties configurations, EventBus eventBus) throws DataProducerPropertyNotDefinedException {
        // Se definen valores default
        int doorId;
        String ipAddress;
        int port;

        // Se valida si existen en las propiedades, si no se deja el valor default
        if (configurations.containsKey(String.format("%s.producer.doorid", configurationId)))
            doorId = Integer.parseInt(configurations.getProperty(String.format("%s.producer.doorid", configurationId)));
        else
            throw new DataProducerPropertyNotDefinedException(String.format("%s.producer.doorid", configurationId));

        if (configurations.containsKey(String.format("%s.producer.ipaddress", configurationId)))
            ipAddress = configurations.getProperty(String.format("%s.producer.ipaddress", configurationId));
        else
            throw new DataProducerPropertyNotDefinedException(String.format("%s.producer.ipaddress", configurationId));

        if (configurations.containsKey(String.format("%s.producer.port", configurationId)))
            port = Integer.parseInt(configurations.getProperty(String.format("%s.producer.port", configurationId)));
        else
            throw new DataProducerPropertyNotDefinedException(String.format("%s.producer.port", configurationId));

        return new HellaAPCECORS485DataProducer(doorId, ipAddress, port, eventBus);
    }

    @Override
    public DataProducer create(Properties configurations, EventBus eventBus) throws DataProducerPropertyNotDefinedException {
        // Se definen valores default
        int doorId;
        String ipAddress;
        int port;

        // Se valida si existen en las propiedades, si no se deja el valor default
        if (configurations.containsKey("producer.doorid"))
            doorId = Integer.parseInt(configurations.getProperty("producer.doorid"));
        else
            throw new DataProducerPropertyNotDefinedException("producer.doorid");

        if (configurations.containsKey("producer.ipaddress"))
            ipAddress = configurations.getProperty("producer.ipaddress");
        else
            throw new DataProducerPropertyNotDefinedException("producer.ipaddress");

        if (configurations.containsKey("producer.port"))
            port = Integer.parseInt(configurations.getProperty("producer.port"));
        else
            throw new DataProducerPropertyNotDefinedException("producer.port");

        return new HellaAPCECORS485DataProducer(doorId, ipAddress, port, eventBus);
    }

    @Override
    public DataProducer create(EventBus EventBus) {
        return new HellaAPCECORS485DataProducer(1, "10.42.1.221", 10076, EventBus);
    }
}
