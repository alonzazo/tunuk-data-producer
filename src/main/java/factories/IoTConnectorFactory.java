package factories;

import drivers.IoTConnector.AWSIoTConnector;
import drivers.IoTConnector.IoTConnector;

public class IoTConnectorFactory {

    public enum IoTConnectorType {
        AWSIoTConnector
    }

    private static IoTConnector createAWSIoTConnector(){return new AWSIoTConnector();};

    public static IoTConnector create(IoTConnectorType type) throws Exception {
        switch (type){
            case AWSIoTConnector:
                return createAWSIoTConnector();
            default:
                throw new Exception("IoTConnector is not well specified.");
        }
    }

}
