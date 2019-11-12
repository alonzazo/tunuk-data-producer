package factories;

import connectors.AWSIoTConnector.AWSIoTConnector;
import connectors.IoTConnector;
import connectors.StandardOutputConnector.StandardOutputConnector;

public class IoTConnectorFactory {

    private static IoTConnector createAWSIoTConnector(){return new AWSIoTConnector();};

    private static IoTConnector createStandardOutputConnector(){return new StandardOutputConnector();}

    public static IoTConnector create(IoTConnectorType type) throws Exception {
        switch (type){
            case AMAZON_WEB_SERVICES:
                return createAWSIoTConnector();
            case STANDARD_OUTPUT:
                return createStandardOutputConnector();
            default:
                throw new Exception("IoTConnector is not well specified.");
        }
    }

}
