import com.amazonaws.services.iot.client.AWSIotQos;
import drivers.AWSIoTConnector;
import drivers.AWSIoTConnectorException;
import drivers.IoTConnector;

public class DataProducer {

    private static final String TestTopic = "sdk/test/java";
    private static final AWSIotQos TestTopicQos = AWSIotQos.QOS0;

    public static void main(String args[]){

        try {

            IoTConnector ioTConnector = new AWSIoTConnector();

            ioTConnector.configure(DataProducer.class.getResource("configurations/aws-config.properties").getPath());

            ioTConnector.connect();

            ioTConnector.start();

            ioTConnector.close();

        }catch (AWSIoTConnectorException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}