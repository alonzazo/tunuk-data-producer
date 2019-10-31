package drivers.AWSIoTConnector;

import com.amazonaws.services.iot.client.AWSIotException;
import drivers.IoTException;

public class AWSIoTConnectorException extends AWSIotException implements IoTException {
    public AWSIoTConnectorException(Throwable cause) {
        super(cause);
    }

}
