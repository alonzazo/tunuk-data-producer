import com.amazonaws.services.iot.client.AWSIotQos;
import drivers.GPSConnector.Dell3003GPSConnector;
import drivers.GPSConnector.GPSConnector;
import drivers.IoTConnector.AWSIoTConnector;
import drivers.IoTConnector.AWSIoTConnectorException;
import drivers.IoTConnector.IoTConnector;
import factories.GPSConnectorFactory;
import factories.IoTConnectorFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class DataProducer {

    private static final String TestTopic = "sdk/test/java";
    private static final AWSIotQos TestTopicQos = AWSIotQos.QOS0;

    public static void main(String args[]){

        try {

            // ------------------------------------------------------------------------------------- CONFIGURE IOTSERVER
            /*IoTConnector ioTConnector = IoTConnectorFactory.create(IoTConnectorFactory.IoTConnectorType.AWSIoTConnector);

            ioTConnector.configure(DataProducer.class.getResource("configurations/aws-config.properties").getPath());

            // -------------------------------------------------------------------------------------CONNECT TO IOTSERVER
            ioTConnector.connect();*/

            // ---------------------------------------------------------------------------------GATHER DATA FROM SENSORS
            // Gather GPS data
            GPSConnector gpsConnector = GPSConnectorFactory.create(GPSConnectorFactory.GPSConnectorType.Dell3003GPSConnector);
            Map<String, String> gpsData = gpsConnector.getCurrentData();

            // Gather CPS data

            // Gather seniors data

            // Gather CAN-BUS data

            // --------------------------------------------------------------------------------------COMPOSE THE MESSAGE
            // Get the current TimeStamp
            String currentTimestamp = getTime();

            // ------------------------------------------------------------------------TRANSMIT THE MESSAGE TO IOTSERVER
            /*ioTConnector.publish("my/own/topic", "estamos trasmitiendo");

            // -----------------------------------------------------------------------------------------CLOSE CONNECTION
            ioTConnector.close();*/

        }catch (AWSIoTConnectorException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static String getTime(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
}