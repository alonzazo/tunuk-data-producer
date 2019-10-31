package drivers;

import drivers.AWSIoTConnector.AWSIoTConnectorException;

public interface IoTConnector {
    void connect() throws AWSIoTConnectorException;
    void reset() throws AWSIoTConnectorException;
    void configure(String propertiesPath) throws AWSIoTConnectorException;
    void publish(String topic, String message) throws AWSIoTConnectorException;
    void close() throws AWSIoTConnectorException;
}
