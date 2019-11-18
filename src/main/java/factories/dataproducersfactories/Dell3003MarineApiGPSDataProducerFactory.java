package factories.dataproducersfactories;

import producers.DataProducer;
import producers.GPSDataProducer.Dell3003MarineApiGPSDataProducer;
import utils.DataBus;

import java.util.Properties;

public class Dell3003MarineApiGPSDataProducerFactory implements DataProducerFactory {
    @Override
    public DataProducer create(Properties properties, DataBus dataBus) {
        return new Dell3003MarineApiGPSDataProducer();
    }

    @Override
    public DataProducer create(DataBus dataBus) {
        return new Dell3003MarineApiGPSDataProducer();
    }

}
