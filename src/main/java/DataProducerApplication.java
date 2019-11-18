import absfactories.DataProducerAbsFactory;
import absfactories.EventBusAbsFactory;
import connectors.IoTConnector;
import consumers.IoTDataBusPublisher;
import consumers.Subscriber;
import factories.*;
import javafx.util.Pair;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import producers.DataProducer;
import utils.DataBus;
import utils.EventBus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

public class DataProducerApplication {
    /*public static final Logger log = LogManager.getLogger("ConsoleFile");*/

    public static void main(String args[]){

        try {

            List<String> argList = Arrays.asList(args);
            /*DataBus dataBus = DataBusFactory.create(DataBusType.SYNCRONIZED);*/
            List<Pair<String, Properties>> producersConfigurations;

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
                producersConfigurations = loadProducersConfigurations(DataProducerApplication.class.getResource("configurations/producers-config.properties").getPath());
            }else {
                ioTConnector.configure("configurations/aws-config.properties");
                producersConfigurations = loadProducersConfigurations("configurations/producers-config.properties");
            }


            // -------------------------------------------------------------------------------------CONNECT TO IOTSERVER
            ioTConnector.connect();
            // ---------------------------------------------------------------------------------GATHER DATA FROM SENSORS

            // Gather CAN-BUS data
            List<DataProducer> dataProducerList = new LinkedList<>();
            Map<String,EventBus> dataBusMap = new HashMap<>();

            // DataProducers gathering initialization
            for (Pair<String, Properties> producerConfiguration:
                 producersConfigurations) {
                try {

                    EventBus currentEventBus;
                    // Se revisa qué tipo de databus ocupa el productor
                    // Si es micro batch
                    if (producerConfiguration.getValue().containsKey("databus.mode") &&
                        producerConfiguration.getValue().getProperty("databus.mode").equals("microbatch")){

                        // Se obtiene el intervalo
                        Long intervalMilis = 1000L; // Valor default //Cast
                        if (producerConfiguration.getValue().containsKey( "databus.interval" ))
                            intervalMilis = Long.valueOf(producerConfiguration.getValue().getProperty("databus.interval"));

                        // Se revisa si ya existe uno de ese intervalo tiempo (en unidades de milisegundos)
                        if (dataBusMap.containsKey(intervalMilis.toString()))
                            currentEventBus = dataBusMap.get(intervalMilis.toString());
                        // Si no, se crea uno nuevo y se asigna como
                        else {
                            MicrobatchDataBusFactory microbatchDataBusFactory = (MicrobatchDataBusFactory) EventBusAbsFactory.create(EventBusType.MICROBATCH); //TODO Buscar la forma de no usar casting
                            currentEventBus = microbatchDataBusFactory.create(intervalMilis);
                            dataBusMap.put(intervalMilis.toString(), currentEventBus);
                            ((DataBus) currentEventBus).startPublication();
                        }

                    } // Si no se asigna como streaming
                    else {
                        // Se asigna el databus de streaming
                        if (dataBusMap.containsKey( "streaming" ))
                            currentEventBus = dataBusMap.get("streaming");
                        else {
                            EventBusFactory streamingDataBusFactory = EventBusAbsFactory.create(EventBusType.STREAMING);
                            currentEventBus = streamingDataBusFactory.create();
                            dataBusMap.put( "streaming", currentEventBus );
                        }
                    }

                    // Se crea el productor de dato y se le asigna el databus
                    DataProducer dataProducer = DataProducerAbsFactory
                            .createFactory(producerConfiguration.getKey())
                            .create(producerConfiguration.getValue(), currentEventBus);

                    // Se inicia la producción
                    dataProducer.startProduction();

                    // Se añade a la lista de productores de datos
                    dataProducerList.add(dataProducer);

                }catch (Exception ex){
                    ex.printStackTrace();
                    System.out.println(ex.getMessage());
                }
            }

            // ---------------------------------------------------------------------------FILTER AND COMPOSE THE MESSAGE

            Function<List<Map<String,String>>,String> composerFunction = (currentDataBus) -> {
                // Se filtra el dataBus

                // Se compone el mensaje
                return composeMessage(identity, currentDataBus);
            };

            // ------------------------------------------------------------------------TRANSMIT THE MESSAGE TO IOTSERVER


            Subscriber dataBusPublisher = new IoTDataBusPublisher(ioTConnector, "dell3003test", composerFunction);
            dataBusMap.values().forEach((eventBus) -> eventBus.subscribe(dataBusPublisher));


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

    private static List<Pair<String, Properties>> loadProducersConfigurations(String filePath) throws FileNotFoundException {

        // Se carga el archivo
        FileReader fileReader = new FileReader(filePath);

        // Se monta en un Scanner que separe cada linea por un '\n'
        Scanner scanner = new Scanner(fileReader);
        scanner.useDelimiter("\n");

        // Inicializamos contenedores y variables
        List<Pair<String, Properties>> producersList = new LinkedList<>();
        int LINES_LIMIT = 1024;

        Properties currentProperties = new Properties();
        String currentProducerName = "";

        // Se parsea por cada linea
        for (int i = 0; scanner.hasNext() && i < LINES_LIMIT; i++){
            String line = scanner.next();

            // Si la linea comienza con un # es un comentario, se ignora
            if (line.length() > 0 && line.trim().charAt(0) == '#'){
                continue;
            }
            // Si corresponde a un par llave valor se agrega al currentProperties
            else if (line.matches("[^=]+=[^=]*")){
                String[] keyValue = line.split("=");
                currentProperties.put( keyValue[0].trim(), keyValue[1].trim() );

                // Si al leer es la última línea permitida o del archivo se agrega como un par Nombre de productor y sus propiedades
                if (i == LINES_LIMIT - 1 || !scanner.hasNext()){
                    producersList.add(new Pair<>(currentProducerName,currentProperties));
                }
            }
            // Si la linea no esta vacia, es decir, es un nombre nuevo de productor entonces
            else if (!line.trim().equals("")){
                // Si el nombre del productor no esta vacio
                if (!currentProducerName.equals("")){
                    // Se agrega un par Nombre de productor y sus propiedades
                    producersList.add(new Pair<>(currentProducerName,currentProperties));
                    currentProperties = new Properties();
                }
                // Finalmente, el nuevo nombre de productor se asigna como Nombre de productor actual
                currentProducerName = line.trim();
            }
        }
        return producersList;
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