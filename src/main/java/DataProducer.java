import drivers.IoTConnector.IoTConnector;
import factories.*;
import drivers.GPSConnector.GPSConnector;
import drivers.IoTConnector.AWSIoTConnectorException;
import drivers.APCConnector.APCConnector;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class DataProducer {

    public static void main(String args[]){

        try {

            // ------------------------------------------------------------------------------------- CONFIGURE IOTSERVER
            IoTConnector ioTConnector = IoTConnectorFactory.create(IoTConnectorType.AMAZON_WEB_SERVICES);

            ioTConnector.configure(DataProducer.class.getResource("configurations/aws-config.properties").getPath());

            // -------------------------------------------------------------------------------------CONNECT TO IOTSERVER
            ioTConnector.connect();

            // ---------------------------------------------------------------------------------GATHER DATA FROM SENSORS
            // Gather GPS data
            GPSConnector gpsConnector = GPSConnectorFactory.create(GPSConnectorType.DELL_3003);
            Map<String, String> gpsData = gpsConnector.getCurrentData();

            // Gather AutomaticPeopleCounter(APC) data
            APCConnector apcConnector = APCConnectorFactory.create(APCConnectorType.Hella_APC_ECO_RS485, 1, "192.168.1.1", 10076);
            Map<String,String> apcData = apcConnector.getData();

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