package drivers;

public interface IoTConnector {
    void connect() throws AWSIoTConnectorException;
    void reset() throws  AWSIoTConnectorException;
    void configure(String propertiesPath) throws  AWSIoTConnectorException;
    void start() throws AWSIoTConnectorException;
    void close() throws AWSIoTConnectorException;
}
