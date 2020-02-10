import absfactories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import connectors.IoTConnector;
import eventbuses.DataBus;
import eventbuses.EventBus;
import factories.IoTConnectorFactory;
import factories.IoTConnectorType;
import factories.eventbusesfactories.EventBusFactory;
import factories.eventbusesfactories.EventBusType;
import factories.eventbusesfactories.MicrobatchDataBusFactory;
import factories.persistentqueuesfactories.BerkleyDBPersistentQueueFactory;
import factories.persistentqueuesfactories.PersistentQueueFactory;
import factories.subscribersfactories.IoTDataBusPublisherFactory;
import factories.subscribersfactories.SubscriberFactory;
import faulttolerance.PersistentQueue;
import org.json.JSONObject;
import producers.DataProducer;
import subscribers.Subscriber;

import java.io.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;

public class DataProducerApplication {
    /*public static final Logger log = LogManager.getLogger("ConsoleFile");*/
    private static final String IDENTITY_FILE_PATH = "configurations/identity.properties";
    private static Properties identity;

    public static void main(String args[]){

        try {

            List<String> argList = Arrays.asList(args);
            List<Map.Entry<String, Properties>> producersConfigurations;

            // -------------------------------------------------------------------------------------------- INITIALIZING

            // Cargar datos de identidad del bus
            if (argList.contains("--debug")){
                try {
                    identity = loadIdentityFile(DataProducerApplication.class.getResource("").getPath() + IDENTITY_FILE_PATH);
                } catch (Exception ex){
                    identity = createNewIdentityFile(DataProducerApplication.class.getResource("").getPath() + IDENTITY_FILE_PATH);
                }
            } else {
                try {
                    identity = loadIdentityFile(IDENTITY_FILE_PATH);
                } catch (Exception ex){
                    identity = createNewIdentityFile(IDENTITY_FILE_PATH);
                }
            }

            // ------------------------------------------------------------------------------------- CONFIGURE IOTSERVER
            IoTConnector ioTConnector;
            if (argList.contains("--stdout")){
                ioTConnector = IoTConnectorFactory.create(IoTConnectorType.STANDARD_OUTPUT);
            } else if (argList.contains("--aws")){
                ioTConnector = IoTConnectorFactory.create(IoTConnectorType.AMAZON_WEB_SERVICES);
            } else {
                ioTConnector = IoTConnectorFactory.create(IoTConnectorType.KAFKA_PRODUCER);
            }

            if (argList.contains("--debug")){
                //ioTConnector.configure(DataProducerApplication.class.getResource("configurations/aws-config.properties").getPath());
                producersConfigurations = loadProducersConfigurations(DataProducerApplication.class.getResource("configurations/producers-config.properties").getPath());
            } else if (argList.contains("--aws")){
                ioTConnector.configure("configurations/aws-config.properties");
                producersConfigurations = loadProducersConfigurations("configurations/producers-config.properties");
            } else {
                producersConfigurations = loadProducersConfigurations("configurations/producers-config.properties");
            }


            // -------------------------------------------------------------------------------------CONNECT TO IOTSERVER
            ioTConnector.connect();
            // ---------------------------------------------------------------------------------GATHER DATA FROM SENSORS

            Map<String,EventBus> eventBusMap = new HashMap<>();
            List<DataProducer> dataProducerList = new LinkedList<>();

            // DataProducers and eventBusMap gathering initialization
            initializeDataProducers(producersConfigurations, eventBusMap, dataProducerList);

            // ---------------------------------------------------------------------------FILTER AND COMPOSE THE MESSAGE

            Function<List<Map<String,String>>,String> composerFunction = (currentDataBus) -> {
                // Se filtra el dataBus
                List<Map<String, String>> dataFiltered = filterJustOneMessageByScheme(currentDataBus);
                // Se compone el mensaje
                return composeMessage(identity, dataFiltered);
            };

            // -----------------------------------------------------------------------------------FAULT TOLERANCE SYSTEM

            PersistentQueueFactory persistentQueueFactory = PersistentQueueAbsFactory.createFactory(PersistentQueueType.BERKLEY_DB);
            ((BerkleyDBPersistentQueueFactory) persistentQueueFactory)
                    .setQueueEnvironmentPath("data-backups")
                    .setQueueName("IoTQueueBackup")
                    .setCacheSize(10);

            PersistentQueue persistentQueue = persistentQueueFactory.create();

            // ------------------------------------------------------------------------TRANSMIT THE MESSAGE TO IOTSERVER

            SubscriberFactory subscriberFactory = SubscriberAbsFactory.createFactory(SubscriberType.IOT_DATA_BUS_PUBLISHER);
            ((IoTDataBusPublisherFactory) subscriberFactory)
                    .setIoTConnector(ioTConnector)
                    .setTopic(argList.indexOf("--topic") != -1 ? argList.get(argList.indexOf("--topic") + 1): "data-producers-" + identity.getProperty("id"))
                    .setHandlerFunction(composerFunction)
                    .setPersistentQueue(persistentQueue);

            Subscriber dataBusPublisher = subscriberFactory.create();
            eventBusMap.values().forEach((eventBus) -> eventBus.subscribe(dataBusPublisher));


            // -----------------------------------------------------------------------------------------CLOSE CONNECTION
            //ioTConnector.close();

        } catch (Exception e){

            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static List<Map<String, String>> filterJustOneMessageByScheme(List<Map<String, String>> dataBusList) {
        HashSet<String> schemesSet = new HashSet<>();
        List<Map<String, String>> result = new LinkedList<>();

        dataBusList.forEach( (dataBus) -> {
            if (dataBus.containsKey("data-scheme"))
                if (!schemesSet.contains(dataBus.get("data-scheme"))){
                    result.add(dataBus);
                    schemesSet.add(dataBus.get("data-scheme"));
                }
        });

        return result;

    }

    private static void initializeDataProducers(List<Map.Entry<String, Properties>> producersConfigurations, Map<String, EventBus> eventBusMap, List<DataProducer> dataProducerList){
        for (Map.Entry<String, Properties> producerConfiguration:
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
                    if (eventBusMap.containsKey(intervalMilis.toString()))
                        currentEventBus = eventBusMap.get(intervalMilis.toString());
                        // Si no, se crea uno nuevo y se asigna como
                    else {
                        MicrobatchDataBusFactory microbatchDataBusFactory = (MicrobatchDataBusFactory) EventBusAbsFactory.create(EventBusType.MICROBATCH); //TODO Buscar la forma de no usar casting
                        currentEventBus = microbatchDataBusFactory.create(intervalMilis);
                        eventBusMap.put(intervalMilis.toString(), currentEventBus);
                        ((DataBus) currentEventBus).startPublication();
                    }

                } // Si no se asigna como streaming
                else {
                    // Se asigna el databus de streaming
                    if (eventBusMap.containsKey( "streaming" ))
                        currentEventBus = eventBusMap.get("streaming");
                    else {
                        EventBusFactory streamingDataBusFactory = EventBusAbsFactory.create(EventBusType.STREAMING);
                        currentEventBus = streamingDataBusFactory.create();
                        eventBusMap.put( "streaming", currentEventBus );
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
    }

    private static Properties createNewIdentityFile(String path) {
        Properties properties = null;
        try {
            //Creamos el archivo
            File file = new File(path);
            file.createNewFile();

            // Se genera el UUID
            String id = UUID.randomUUID().toString();

            // Inicializamos el impresor
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.print(id);
            printWriter.close();

            // Se crea las propiedades
            properties = new Properties();
            properties.put("id", id);

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return properties;

    }

    private static Properties loadIdentityFile(String identityFilePath) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(identityFilePath);
        Scanner scanner = new Scanner(fileInputStream);

        String id;
        if (scanner.hasNext()){
            id = scanner.nextLine();
            try {
                UUID.fromString(id);
            } catch (Exception ex){
                throw new Exception("Invalid identity structure");
            }

        }
        else
            throw new Exception("Invalid identity file");

        Properties properties = new Properties();
        properties.put("id", id);

        return properties;
    }

    private static List<Map.Entry<String, Properties>> loadProducersConfigurations(String filePath) throws FileNotFoundException {

        // Se carga el archivo
        FileReader fileReader = new FileReader(filePath);

        // Se monta en un Scanner que separe cada linea por un '\n'
        Scanner scanner = new Scanner(fileReader);
        scanner.useDelimiter("\n");

        // Inicializamos contenedores y variables
        List<Map.Entry<String, Properties>> producersList = new LinkedList<>();
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
                    producersList.add(new AbstractMap.SimpleEntry<>(currentProducerName,currentProperties));
                }
            }
            // Si la linea no esta vacia, es decir, es un nombre nuevo de productor entonces
            else if (!line.trim().equals("")){
                // Si el nombre del productor no esta vacio
                if (!currentProducerName.equals("")){
                    // Se agrega un par Nombre de productor y sus propiedades
                    producersList.add(new AbstractMap.SimpleEntry<>(currentProducerName,currentProperties));
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
        //String currentTimestamp = getTime();

        // Creamos un json array a partir de los lista de los mapas datos
        //String jsonDataArray = getJsonDataArray(dataList);

        //Componemos el JsonFinal
        JSONObject jsonObject = new JSONObject();
        List<JSONObject> jsonDataList = new LinkedList<>();

        for (Map<String, String> dataElement: dataList)
            jsonDataList.add(new JSONObject(dataElement));

        try {
            jsonObject.put("id",identity.getProperty("id"));
            jsonObject.put("date", LocalDate.from(getTime()));
            jsonObject.put("timestamp",getTime());
            jsonObject.put("data",jsonDataList);
        } catch (Exception ignored){
            ignored.printStackTrace();
        }

        return jsonObject.toString();
    }

    private static OffsetDateTime getTime(){
        return OffsetDateTime.now();
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