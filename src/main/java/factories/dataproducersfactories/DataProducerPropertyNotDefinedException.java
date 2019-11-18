package factories.dataproducersfactories;

public class DataProducerPropertyNotDefinedException extends Exception {

    public DataProducerPropertyNotDefinedException(String propertyNotDefined) {
        super("DataProducer property was not well defined: " + propertyNotDefined);
    }
}
