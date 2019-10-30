package factories;

import drivers.IoTConnector.AWSIoTConnector;
import drivers.IoTConnector.IoTConnector;

public class IoTConnectorFactory {

    private static IoTConnector createAWSIoTConnector(){return new AWSIoTConnector();};

    public static IoTConnector create(IoTConnectorType type) throws Exception {
        switch (type){
            case AMAZON_WEB_SERVICES:
                return createAWSIoTConnector();
            default:
                throw new Exception("IoTConnector is not well specified.");
        }
    }

}
