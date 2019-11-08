package EventListener;

import drivers.AWSIoTConnector.AWSIoTConnectorException;
import org.json.JSONException;

import java.io.IOException;
import java.util.Map;

public interface EventListener {
    void listen(Map<String,String> data) throws AWSIoTConnectorException, IOException, JSONException;
}
