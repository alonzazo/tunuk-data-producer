package factories.dataproducersfactories;

import producers.DataProducer;
import utils.DataBus;

import java.util.Properties;

public interface DataProducerFactory {

    DataProducer create(Properties properties, DataBus dataBus) throws DataProducerPropertyNotDefinedException;
    DataProducer create(DataBus dataBus);

}
