package absfactories;

public class DataProducerNotFoundException extends Exception {
    public DataProducerNotFoundException(String dataProducerNotFound) {
        super("DataProducerType not found: " + dataProducerNotFound);
    }

    public DataProducerNotFoundException(String dataProducerNotFound, Throwable cause) {
        super("DataProducerType not found: " + dataProducerNotFound, cause);
    }
}
