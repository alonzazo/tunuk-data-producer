package connectors;

public class IoTConnectorException extends Exception {
    public IoTConnectorException(Exception e) {
        initCause(e);
    }
}
