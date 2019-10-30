package drivers.IoTConnector;

import com.amazonaws.services.iot.client.AWSIotException;

public class AWSIoTConnectorException extends AWSIotException implements IoTException {
    public AWSIoTConnectorException(Throwable cause) {
        super(cause);
    }

}
