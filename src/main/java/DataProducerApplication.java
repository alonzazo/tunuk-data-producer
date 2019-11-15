import connectors.IoTConnector;
import consumers.DataBusPublisher;
import consumers.IoTDataBusPublisher;
import factories.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import producers.DataProducer;
import utils.DataBus;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

public class DataProducerApplication {
    /*public static final Logger log = LogManager.getLogger("ConsoleFile");*/

    public static void main(String args[]){

        try {

            List<String> argList = Arrays.asList(args);
            DataBus dataBus = DataBusFactory.create(DataBusType.SYNCRONIZED);

            // -------------------------------------------------------------------------------------------- INITIALIZING
            Properties identity;
            // Cargar datos de identidad del bus
            if (argList.contains("--debug")){
                identity = loadFileProperties(DataProducerApplication.class.getResource("configurations/identity.properties").getPath());
            }else {
                identity = loadFileProperties("configurations/identity.properties");
            }

            // ------------------------------------------------------------------------------------- CONFIGURE IOTSERVER
            IoTConnector ioTConnector;
            if (argList.contains("--stdout")){
                ioTConnector = IoTConnectorFactory.create(IoTConnectorType.STANDARD_OUTPUT);
            } else {
                ioTConnector = IoTConnectorFactory.create(IoTConnectorType.AMAZON_WEB_SERVICES);
            }

            if (argList.contains("--debug")){
                ioTConnector.configure(DataProducerApplication.class.getResource("configurations/aws-config.properties").getPath());
            }else {
                ioTConnector.configure("configurations/aws-config.properties");
            }


            // -------------------------------------------------------------------------------------CONNECT TO IOTSERVER
            ioTConnector.connect();
            // ---------------------------------------------------------------------------------GATHER DATA FROM SENSORS

            // Gather GPS data
            /*DataProducer gpsDataProducer = DataProducerFactory.create(DataProducerType.DELL_3003_RXTX, dataBus);
            gpsDataProducer.startProduction();*/

            DataProducer gpsDataProducer = DataProducerFactory.create(DataProducerType.HONGDIAN_H8922S, dataBus);
            gpsDataProducer.startProduction();


            // Gather AutomaticPeopleCounter(APC) data
            DataProducer apcDataProducer1 = DataProducerFactory.create(DataProducerType.Hella_APC_ECO_RS485, dataBus,1,"192.168.8.221",10076);
            apcDataProducer1.startProduction();

            DataProducer apcDataProducer2 = DataProducerFactory.create(DataProducerType.Hella_APC_ECO_RS485, dataBus,2,"192.168.8.222",10076);
            apcDataProducer2.startProduction();

            // Gather CAN-BUS data
            // ---------------------------------------------------------------------------FILTER AND COMPOSE THE MESSAGE

            Function<DataBus,String> composerFunction = (currentDataBus) -> {
                // Se filtra el dataBus

                // Se compone el mensaje
                return composeMessage(identity, currentDataBus.consumeMergedData());
            };

            // ------------------------------------------------------------------------TRANSMIT THE MESSAGE TO IOTSERVER

            DataBusPublisher dataBusPublisher = new IoTDataBusPublisher(dataBus, ioTConnector, "dell3003test", composerFunction);
            dataBusPublisher.startPublish();


            // -----------------------------------------------------------------------------------------CLOSE CONNECTION
            //ioTConnector.close();

        } catch (Exception e){

            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static Properties loadFileProperties(String propertiesPathFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(propertiesPathFile);

        Properties properties = new Properties();
        properties.load(fileInputStream);

        return properties;
    }

    private static String composeMessage(Properties identity, List<Map<String, String>> dataList) {
        // Get the current TimeStamp
        String currentTimestamp = getTime();

        // Creamos un json array a partir de los lista de los mapas datos
        String jsonDataArray = getJsonDataArray(dataList);

        //Componemos el JsonFinal
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("eventId",identity.getProperty("eventId"));
            jsonObject.put("busId",identity.getProperty("busId"));
            jsonObject.put("companyId",identity.getProperty("companyId"));
            jsonObject.put("deviceId",identity.getProperty("deviceId"));
            jsonObject.put("timestamp",currentTimestamp);
            jsonObject.put("data",jsonDataArray);
        } catch (Exception ignored){}

        return jsonObject.toString();
    }

    private static String getTime(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    private static String getJsonDataArray(List<Map<String,String>> dataList) {
        ObjectMapper mapToJsonMapper = new ObjectMapper();

        StringBuilder dataArray = new StringBuilder();

        for (Map<String, String> data: dataList){
            try {
                dataArray.append(mapToJsonMapper.writeValueAsString(data)).append(",");
            } catch (Exception ignored){ }
        }
        if (dataArray.length() != 0)
            return "[" + dataArray.substring(0, dataArray.length() - 1) + "]";
        else
            return "[]";
    }
}