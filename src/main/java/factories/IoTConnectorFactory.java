package factories;

import connectors.AWSIoTConnector.AWSIoTConnector;
import connectors.IoTConnector;
import connectors.KafkaProducerConnector.KafkaProducerConnector;
import connectors.StandardOutputConnector.StandardOutputConnector;

public class IoTConnectorFactory {

    private static IoTConnector createAWSIoTConnector(){return new AWSIoTConnector();};

    private static IoTConnector createStandardOutputConnector(){return new StandardOutputConnector();}

    private static IoTConnector createKafkaProducerConnector(){ try {return new KafkaProducerConnector();} catch (Exception e) { return null; }}

    public static IoTConnector create(IoTConnectorType type) throws Exception {
        switch (type){
            case AMAZON_WEB_SERVICES:
                return createAWSIoTConnector();
            case STANDARD_OUTPUT:
                return createStandardOutputConnector();
            case KAFKA_PRODUCER:
                return createKafkaProducerConnector();
            default:
                throw new Exception("IoTConnector is not well specified.");
        }
    }

}
