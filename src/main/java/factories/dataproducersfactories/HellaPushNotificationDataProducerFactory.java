package factories.dataproducersfactories;

import eventbuses.EventBus;
import producers.APCDataProducer.HellaPushNotificationDataProducer;
import producers.DataProducer;

import java.util.Properties;

public class HellaPushNotificationDataProducerFactory implements DataProducerFactory {

    @Override
    public DataProducer create(String configurationId, Properties configurations, EventBus eventBus) throws DataProducerPropertyNotDefinedException {
        // Se definen valores default
        int port = 80;
        String[] managedDevices = new String[0];

        // Se valida si existen en las propiedades, si no se deja el valor default
        if (configurations.containsKey(String.format("%s.producer.port", configurationId)))
            port = Integer.parseInt(configurations.getProperty(String.format("%s.producer.port", configurationId)));

        if (configurations.containsKey(String.format("%s.producer.devices", configurationId)))
            managedDevices = configurations.getProperty(String.format("%s.producer.devices", configurationId)).split(",");

        return new HellaPushNotificationDataProducer(eventBus, port, managedDevices);
    }

    @Override
    public DataProducer create(Properties configurations, EventBus eventBus) throws DataProducerPropertyNotDefinedException {
        // Se definen valores default
        int port = 80;
        String[] managedDevices = new String[0];

        // Se valida si existen en las propiedades, si no se deja el valor default
        if (configurations.containsKey("producer.port"))
            port = Integer.parseInt(configurations.getProperty("producer.port"));

        if (configurations.containsKey("producer.devices"))
            managedDevices = configurations.getProperty("producer.devices").split(",");

        return new HellaPushNotificationDataProducer(eventBus, port, managedDevices);
    }

    @Override
    public DataProducer create(EventBus EventBus) {
        return new HellaPushNotificationDataProducer(EventBus);
    }
}
