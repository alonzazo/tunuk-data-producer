package subscribers;

import connectors.IoTConnectorException;

import java.util.List;
import java.util.Map;

public interface Subscriber {

    void initialize() throws IoTConnectorException;

    void handleDataBus(List<Map<String,String>> data);

    void finish() throws IoTConnectorException;

}
