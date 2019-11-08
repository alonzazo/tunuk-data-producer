import drivers.IoTConnector;
import factories.*;
import drivers.DataDriver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataProducer {
    private static final Logger log = LogManager.getLogger("ConsoleFile");

    public static void main(String args[]){

        try {

            List<String> argList = Arrays.asList(args);

            // -------------------------------------------------------------------------------------------- INITIALIZING
            Properties identity;
            // Cargar datos de identidad del bus
            if (argList.contains("--debug")){
                identity = loadFileProperties(DataProducer.class.getResource("configurations/identity.properties").getPath());
            }else {
                identity = loadFileProperties("configurations/identity.properties");
            }
            // ------------------------------------------------------------------------------------- CONFIGURE IOTSERVER
            IoTConnector ioTConnector = IoTConnectorFactory.create(IoTConnectorType.AMAZON_WEB_SERVICES);

            if (argList.contains("--debug")){
                ioTConnector.configure(DataProducer.class.getResource("configurations/aws-config.properties").getPath());
            }else {
                ioTConnector.configure("configurations/aws-config.properties");
            }


            // -------------------------------------------------------------------------------------CONNECT TO IOTSERVER
            ioTConnector.connect();
            // ---------------------------------------------------------------------------------GATHER DATA FROM SENSORS
            List<Map<String,String>> dataList = new LinkedList<>();
/*
            boolean stopFlag = false;
            while (!stopFlag){*/

                // Gather GPS data
                if (argList.contains("--marine-api")) {
                    DataDriver gpsDataDriver = ConnectorFactory.create(ConnectorType.DELL_3003_MARINE_API);
                    dataList.add(gpsDataDriver.getData());
                }else if (argList.contains("--rxtx")){
                    DataDriver gpsDataDriver = ConnectorFactory.create(ConnectorType.DELL_3003_RXTX, (gpsData) -> {
                        dataList.add(gpsData);
                        // --------------------------------------------------------------------------------------COMPOSE THE MESSAGE
                        // Se compone el mensaje
                        String message = composeMessage(identity, dataList);
                        System.out.println(message);
                        // ------------------------------------------------------------------------TRANSMIT THE MESSAGE TO IOTSERVER
                        ioTConnector.publish("dell3003test", message);

                        dataList.clear();

                    });
                    gpsDataDriver.getData();
                } else {
                    DataDriver gpsDataDriver = ConnectorFactory.create(ConnectorType.DELL_3003_ARCGIS,"/dev/ttyHS0");
                    dataList.add(gpsDataDriver.getData());
                }

                // Gather AutomaticPeopleCounter(APC) data
                /*DataDriver apcDataDriver = ConnectorFactory.create(ConnectorType.Hella_APC_ECO_RS485);
                dataList.add(apcDataDriver.getData());*/

                // Gather CAN-BUS data
                // --------------------------------------------------------------------------------------COMPOSE THE MESSAGE
                // Se compone el mensaje
                /*String message = composeMessage(identity, dataList);
                System.out.println(message);
                // ------------------------------------------------------------------------TRANSMIT THE MESSAGE TO IOTSERVER
                ioTConnector.publish("dell3003test", message);

                dataList.clear();
                Thread.sleep(1000); // Wait a second.*/
            /*}*/
            // -----------------------------------------------------------------------------------------CLOSE CONNECTION
            //ioTConnector.close();

        } catch (Exception e){

            e.printStackTrace();
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }
    }

    private static Properties loadFileProperties(String propertiesPathFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(propertiesPathFile);

        Properties properties = new Properties();
        properties.load(fileInputStream);

        return properties;
    }

    private static String composeMessage(Properties identity, List<Map<String, String>> dataList) throws IOException, JSONException {
        // Get the current TimeStamp
        String currentTimestamp = getTime();

        // Creamos un json array a partir de los lista de los mapas datos
        String jsonDataArray = getJsonDataArray(dataList);

        //Componemos el JsonFinal
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("eventId",identity.getProperty("eventId"));
        jsonObject.put("busId",identity.getProperty("busId"));
        jsonObject.put("companyId",identity.getProperty("companyId"));
        jsonObject.put("deviceId",identity.getProperty("deviceId"));
        jsonObject.put("timestamp",currentTimestamp);
        jsonObject.put("data",jsonDataArray);

        return jsonObject.toString();
    }

    private static String getTime(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    private static String getJsonDataArray(List<Map<String,String>> dataList) throws IOException {
        ObjectMapper mapToJsonMapper = new ObjectMapper();

        StringBuilder dataArray = new StringBuilder();

        for (Map<String, String> data: dataList){
            dataArray.append(mapToJsonMapper.writeValueAsString(data)).append(",");
        }
        if (dataArray.length() != 0)
            return "[" + dataArray.substring(0, dataArray.length() - 1) + "]";
        else
            return "[]";
    }
}