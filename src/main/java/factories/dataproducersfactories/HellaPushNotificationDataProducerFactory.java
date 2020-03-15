package factories.dataproducersfactories;

import eventbuses.EventBus;
import producers.APCDataProducer.HellaPushNotificationDataProducer;
import producers.DataProducer;

import java.util.Properties;

public class HellaPushNotificationDataProducerFactory implements DataProducerFactory {
    @Override
    public DataProducer create(Properties properties, EventBus eventBus) throws DataProducerPropertyNotDefinedException {
        // Se definen valores default
        int port = 80;
        String[] managedDevices = new String[0];

        // Se valida si existen en las propiedades, si no se deja el valor default
        if (properties.containsKey("producer.port"))
            port = Integer.parseInt(properties.getProperty("producer.port"));

        if (properties.containsKey("producer.devices"))
            managedDevices = properties.getProperty("producer.devices").split(",");

        return new HellaPushNotificationDataProducer(eventBus, port, managedDevices);
    }

    @Override
    public DataProducer create(EventBus EventBus) {
        return new HellaPushNotificationDataProducer(EventBus);
    }
}
