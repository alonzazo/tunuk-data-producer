package connectors.StandardOutputConnector;

import connectors.IoTConnector;
import connectors.IoTConnectorException;

public class StandardOutputConnector implements IoTConnector {
    @Override
    public void connect() throws IoTConnectorException {
    }

    @Override
    public void reset() throws IoTConnectorException {
    }

    @Override
    public void configure(String propertiesPath) throws IoTConnectorException {

    }

    @Override
    public void publish(String topic, String message) throws IoTConnectorException {
        System.out.println("TOPIC: " + topic + "\nMESSAGE: \n" + message);
    }

    @Override
    public void close() throws IoTConnectorException {

    }
}
