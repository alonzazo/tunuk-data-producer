package absfactories;

import factories.dataproducersfactories.*;

public class DataProducerAbsFactory {

    private DataProducerAbsFactory(){}

    public static DataProducerFactory createFactory(DataProducerType type) throws DataProducerNotFoundException {
        switch (type){
            case HONGDIAN_H8922S:
                return new HongdianH8922SGPSDataProducerFactory();
            case HELLA_APC_ECO_RS485:
                return new HellaAPCECORS485DataProducerFactory();
            case DELL_3003_RXTX:
                return new Dell3003RxtxGPSDataProducerFactory();
            case DELL_3003_MARINE_API:
                return new Dell3003MarineApiGPSDataProducerFactory();
            case ADAFRUIT_ULTIMATE:
                return new AdafruitUltimateGPSDataProducerFactory();
            case TEST_GPS:
                return new TestGPSDataProducerFactory();
            case TEST_APC:
                return new TestAPCDataProducerFactory();
            default:
                throw new DataProducerNotFoundException(type.toString());
        }
    }

    public static DataProducerFactory createFactory(String type) throws DataProducerNotFoundException {
        DataProducerType dataType;
        try {
            dataType = DataProducerType.valueOf(type);
        }catch (EnumConstantNotPresentException e){
            throw new DataProducerNotFoundException(type, e);
        }
        return createFactory(dataType);
    }
}
