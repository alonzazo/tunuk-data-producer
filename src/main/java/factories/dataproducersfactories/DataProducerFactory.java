package factories.dataproducersfactories;

import producers.DataProducer;
import eventbuses.EventBus;

import java.util.Properties;

public interface DataProducerFactory {

    DataProducer create(String configurationId, Properties configurations, EventBus eventBus) throws DataProducerPropertyNotDefinedException;
    DataProducer create(Properties configurations, EventBus EventBus) throws DataProducerPropertyNotDefinedException;
    DataProducer create(EventBus EventBus);

}
