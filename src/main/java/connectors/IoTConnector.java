package connectors;

public interface IoTConnector {
    void connect() throws IoTConnectorException;
    void reset() throws IoTConnectorException;
    void configure(String propertiesPath) throws IoTConnectorException;
    void publish(String topic, String message) throws IoTConnectorException;
    void close() throws IoTConnectorException;
}
