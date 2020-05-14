package factories.dataproducersfactories;

import eventbuses.EventBus;
import producers.APCDataProducer.TestAPCDataProducer;
import producers.DataProducer;

import java.util.Properties;

public class TestAPCDataProducerFactory implements DataProducerFactory {

    @Override
    public DataProducer create(String configurationId, Properties configurations, EventBus eventBus) throws DataProducerPropertyNotDefinedException {
        return create(eventBus);
    }

    @Override
    public DataProducer create(Properties configurations, EventBus EventBus) throws DataProducerPropertyNotDefinedException {
        return create(EventBus);
    }

    @Override
    public DataProducer create(EventBus EventBus) {
        return new TestAPCDataProducer(EventBus);
    }
}
