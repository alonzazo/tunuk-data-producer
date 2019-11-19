package factories.dataproducersfactories;

import producers.DataProducer;
import eventbuses.EventBus;

import java.util.Properties;

public interface DataProducerFactory {

    DataProducer create(Properties properties, EventBus EventBus) throws DataProducerPropertyNotDefinedException;
    DataProducer create(EventBus EventBus);

}
