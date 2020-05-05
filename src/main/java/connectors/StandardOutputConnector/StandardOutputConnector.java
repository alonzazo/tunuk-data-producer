package connectors.StandardOutputConnector;

import connectors.IoTConnector;
import connectors.IoTConnectorException;

import java.time.Instant;

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
        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + "TOPIC: " + topic + "\nMESSAGE: \n" + message);
    }

    @Override
    public void close() throws IoTConnectorException {

    }
}
