package factories;

import connectors.AWSIoTConnector.AWSIoTConnector;
import connectors.IoTConnector;
import connectors.KafkaProducerConnector.AsyncKafkaProducerConnector;
import connectors.KafkaProducerConnector.KafkaProducerConnector;
import connectors.StandardOutputConnector.StandardOutputConnector;

public class IoTConnectorFactory {

    private static IoTConnector createAWSIoTConnector(){return new AWSIoTConnector();};

    private static IoTConnector createStandardOutputConnector(){return new StandardOutputConnector();}

    private static IoTConnector createKafkaProducerConnector(){ try {return new KafkaProducerConnector();} catch (Exception e) { return null; }}

    private static IoTConnector createKafkaProducerConnector(String arg){ try {return new KafkaProducerConnector(arg);} catch (Exception e) { return null; }}

    private static IoTConnector createAsyncKafkaProducerConnector(){ try {return new AsyncKafkaProducerConnector();} catch (Exception e) { return null; }}

    private static IoTConnector createAsyncKafkaProducerConnector(String arg){ try {return new AsyncKafkaProducerConnector(arg);} catch (Exception e) { return null; }}

    public static IoTConnector create(IoTConnectorType type) throws Exception {
        switch (type){
            case AMAZON_WEB_SERVICES:
                return createAWSIoTConnector();
            case STANDARD_OUTPUT:
                return createStandardOutputConnector();
            case KAFKA_PRODUCER:
                return createKafkaProducerConnector();
            case ASYNC_KAFKA_PRODUCER:
                return createAsyncKafkaProducerConnector();
            default:
                throw new Exception("IoTConnector is not well specified.");
        }
    }

    public static IoTConnector create(IoTConnectorType type, String[] args) throws Exception {
        switch (type){
            case AMAZON_WEB_SERVICES:
                return createAWSIoTConnector();
            case STANDARD_OUTPUT:
                return createStandardOutputConnector();
            case KAFKA_PRODUCER:
                if (args.length == 0)
                    return createKafkaProducerConnector();
                else
                    return createKafkaProducerConnector(args[0]);
            case ASYNC_KAFKA_PRODUCER:
                if (args.length == 0)
                    return createAsyncKafkaProducerConnector();
                else
                    return createAsyncKafkaProducerConnector(args[0]);
            default:
                throw new Exception("IoTConnector is not well specified.");
        }
    }

}
