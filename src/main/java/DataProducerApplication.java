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
import factories.subscribersfactories.SubscriberFactory;
import faulttolerance.PersistentQueue;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import producers.DataProducer;
import subscribers.Subscriber;

import java.io.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;

public class DataProducerApplication {

    public static final Logger log = LoggerFactory.getLogger(DataProducerApplication.class);

    private static final String IDENTITY_FILE_PATH = "configurations/identity.properties";

    public static void main(String[] args) {

        try {

            List<String> argList = Arrays.asList(args);
            Properties configurations;

            // -------------------------------------------------------------------------------------------- INITIALIZING


            if (argList.contains("--debug")) {
                configurations = loadConfigurations(DataProducerApplication.class.getResource("configurations/configuration.properties").getPath());
            } else if (argList.contains("--aws")) {
                configurations = loadConfigurations("configurations/producers-config.properties");
            } else if (argList.contains("--default-config")) {
                configurations = loadConfigurations("configurations/producers-config.properties");
            }else if (argList.contains("--snap-version")) {
                configurations = loadConfigurations(System.getenv("SNAP_DATA")+ "/config.properties");
            }
            else {
                configurations = loadConfigurations();
            }

            System.out.println(String.format("Configurations: %s",configurations));

            // Cargar datos de identidad del bus
            if (!configurations.containsKey("dataproducer.id")){
                if (argList.contains("--debug")) {
                    try {
                        configurations.setProperty("dataproducer.id",loadIdentityFromFile(DataProducerApplication.class.getResource("").getPath() + IDENTITY_FILE_PATH));
                    } catch (Exception ex) {
                        configurations.setProperty("dataproducer.id",createNewIdentityFile(DataProducerApplication.class.getResource("").getPath() + IDENTITY_FILE_PATH));
                    }

                } else {
                    try {
                        configurations.setProperty("dataproducer.id", loadIdentityFromFile(IDENTITY_FILE_PATH));
                    } catch (Exception ex) {
                        configurations.setProperty("dataproducer.id", createNewIdentityFile(IDENTITY_FILE_PATH));
                    }
                }
            }

            // ---------------------------------------------------------------------------------GATHER DATA FROM SENSORS

            Map<String, EventBus> eventBusMap = new HashMap<>();
            List<DataProducer> dataProducerList = new LinkedList<>();

            // DataProducers and eventBusMap gathering initialization
            initializeDataProducersAndEventBuses(configurations, eventBusMap, dataProducerList);

            // Subscribers initialization
            initializeDataSubscribers(configurations, eventBusMap);


        } catch (Exception e) {

            e.printStackTrace();
            log.info(e.getMessage());

        }
    }

    private static List<Map<String, String>> filterJustOneGPSMessageByScheme(List<Map<String, String>> dataBusList) {
        List<Map<String, String>> result = new LinkedList<>();
        boolean gpsFlag = false;

        for (Map<String, String> dataBus : dataBusList) {
            if (dataBus.containsKey("data-scheme")) {
                if (dataBus.get("data-scheme").equals("GPS")) {
                    if (!gpsFlag) {
                        result.add(dataBus);
                        gpsFlag = true;
                    }
                } else {
                    result.add(dataBus);
                }
            }
        }

        return result;

    }

    private static void initializeDataProducersAndEventBuses(List<Map.Entry<String, Properties>> producersConfigurations, Map<String, EventBus> eventBusMap, List<DataProducer> dataProducerList) {

        for (Map.Entry<String, Properties> producerConfiguration :
                producersConfigurations) {
            try {

                EventBus currentEventBus;
                // Se revisa qué tipo de databus ocupa el productor
                // Si es micro batch
                if (producerConfiguration.getValue().containsKey("databus.mode") &&
                        producerConfiguration.getValue().getProperty("databus.mode").equals("microbatch")) {

                    // Se obtiene el intervalo
                    Long intervalMilis = 1000L; // Valor default //Cast
                    if (producerConfiguration.getValue().containsKey("databus.interval"))
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
                    if (eventBusMap.containsKey("streaming"))
                        currentEventBus = eventBusMap.get("streaming");
                    else {
                        EventBusFactory streamingDataBusFactory = EventBusAbsFactory.create(EventBusType.STREAMING);
                        currentEventBus = streamingDataBusFactory.create();
                        eventBusMap.put("streaming", currentEventBus);
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

            } catch (Exception ex) {
                ex.printStackTrace();
                log.info(ex.getMessage());
            }
        }
    }

    private static void initializeDataProducersAndEventBuses(Properties configurations, Map<String, EventBus> eventBusMap, List<DataProducer> dataProducerList) {

        String[] producersConfigs = configurations.getProperty("dataproducer.producers").split(",");

        for (String configurationId : producersConfigs) {

            try {
                EventBus currentEventBus;
                // Se revisa qué tipo de databus ocupa el productor
                // Si es micro batch
                if (configurations.containsKey(String.format("%s.databus.mode", configurationId)) &&
                        configurations.getProperty(String.format("%s.databus.mode", configurationId)).equals("microbatch")) {

                    // Se obtiene el intervalo
                    long intervalMilis = 1000L; // Valor default //Cast
                    if (configurations.containsKey(String.format("%s.databus.interval", configurationId)))
                        intervalMilis = Long.parseLong(configurations.getProperty(String.format("%s.databus.interval", configurationId)));

                    // Se revisa si ya existe uno de ese intervalo tiempo (en unidades de milisegundos)
                    if (eventBusMap.containsKey(Long.toString(intervalMilis)))
                        currentEventBus = eventBusMap.get(Long.toString(intervalMilis));
                        // Si no, se crea uno nuevo y se asigna como
                    else {
                        MicrobatchDataBusFactory microbatchDataBusFactory = (MicrobatchDataBusFactory) EventBusAbsFactory.create(EventBusType.MICROBATCH); //TODO Buscar la forma de no usar casting
                        currentEventBus = microbatchDataBusFactory.create(intervalMilis);
                        eventBusMap.put(Long.toString(intervalMilis), currentEventBus);
                        ((DataBus) currentEventBus).startPublication();
                    }

                } // Si no se asigna como streaming
                else {
                    // Se asigna el databus de streaming
                    if (eventBusMap.containsKey("streaming"))
                        currentEventBus = eventBusMap.get("streaming");
                    else {
                        EventBusFactory streamingDataBusFactory = EventBusAbsFactory.create(EventBusType.STREAMING);
                        currentEventBus = streamingDataBusFactory.create();
                        eventBusMap.put("streaming", currentEventBus);
                    }
                }

                // Se crea el productor de dato y se le asigna el databus
                DataProducer dataProducer = DataProducerAbsFactory
                        .createFactory(configurations.getProperty(String.format("%s.type", configurationId)))
                        .create(configurationId, configurations, currentEventBus);

                // Se inicia la producción
                dataProducer.startProduction();

                // Se añade a la lista de productores de datos
                dataProducerList.add(dataProducer);
            } catch (Exception ex) {
                ex.printStackTrace();
                log.info(ex.getMessage());
            }


        }

    }

    private static void initializeDataSubscribers(Properties configurations, Map<String, EventBus> eventBusMap) {

        String[] subscribersConfigs = configurations.getProperty("dataproducer.subscribers").split(",");

        for (String configurationId : subscribersConfigs) {

            try {
                // ------------------------------------------------------------------------------------- CONFIGURE IOTSERVER
                IoTConnector ioTConnector = IoTConnectorFactory.create(configurationId, IoTConnectorType.valueOf(configurations.getProperty(String.format("%s.subscriber.connector", configurationId))), configurations);

                // ---------------------------------------------------------------------------FILTER AND COMPOSE THE MESSAGE

                Function<List<Map<String, String>>, String> composerFunction = (currentDataBus) -> {
                    // Se filtra el dataBus
                    List<Map<String, String>> dataFiltered = filterJustOneGPSMessageByScheme(currentDataBus);
                    // Se compone el mensaje
                    return composeMessage(String.format("data-producers-%s",configurations.getProperty("dataproducer.id")), dataFiltered);
                };

                // -----------------------------------------------------------------------------------FAULT TOLERANCE SYSTEM

                PersistentQueueFactory persistentQueueFactory = PersistentQueueAbsFactory.createFactory(PersistentQueueType.BERKLEY_DB);
                ((BerkleyDBPersistentQueueFactory) persistentQueueFactory)
                        .setQueueEnvironmentPath("data-backups")
                        .setQueueName(String.format("iot-queue-backup-%s", configurationId))
                        .setCacheSize(0);

                PersistentQueue persistentQueue = persistentQueueFactory.create();

                SubscriberFactory subscriberFactory = SubscriberAbsFactory.createFactory(SubscriberType.valueOf(configurations.getProperty(String.format("%s.type", configurationId))));

                Subscriber subscriber = subscriberFactory.create(ioTConnector, String.format("data-producers-%s",configurations.getProperty("dataproducer.id")), composerFunction, persistentQueue);

                // -------------------------------------------------------------------------------------CONNECT TO IOTSERVER
                subscriber.initialize();

                // ------------------------------------------------------------------------TRANSMIT THE MESSAGE TO IOTSERVER

                if (configurations.containsKey(String.format("%s.databus.mode", configurationId))){
                    if (configurations.getProperty(String.format("%s.databus.mode", configurationId)).equals("streaming")){
                        if (eventBusMap.containsKey("streaming")){
                            eventBusMap.get("streaming").subscribe(subscriber);
                        } else {
                            log.warn("Subscriber {} can not be subscribed to streaming event bus since it does not exist.", configurationId);
                        }
                    } else {
                        if (configurations.containsKey(String.format("%s.databus.interval", configurationId))){
                            if (eventBusMap.containsKey(configurations.getProperty(String.format("%s.databus.interval", configurationId))))
                                eventBusMap.get(configurations.getProperty(String.format("%s.databus.interval", configurationId))).subscribe(subscriber);
                            else
                                log.warn(String.format("Subscriber %s can not be subscribed to %s ms microbatch event bus since it does not exist.", configurationId, configurations.getProperty(String.format("%s.databus.interval", configurationId)) ));
                        } else {
                            if (eventBusMap.containsKey("1000")) {
                                eventBusMap.get("1000").subscribe(subscriber);
                                log.info("Subscriber {} was subscribed to 1000ms microbatch event bus implicitly.", configurationId);
                            }
                            else
                                log.warn(String.format("Subscriber %s can not be subscribed to ms %s microbatch event bus since it does not exist.", configurationId, configurations.getProperty(String.format("%s.databus.interval", configurationId)) ));
                        }
                    }
                } else {
                    if (eventBusMap.containsKey("streaming")){
                        eventBusMap.get("streaming").subscribe(subscriber);
                    } else {
                        log.warn("Subscriber {} was assigned to streaming event bus but can not be subscribed since it does not exist.", configurationId);
                    }
                }
                // -----------------------------------------------------------------------------------------CLOSE CONNECTION
                //subscriber.finish();


            } catch (Exception ex) {
                ex.printStackTrace();
                log.warn(ex.getMessage());
            }
        }
    }

    private static String createNewIdentityFile(String path) {
        String id = null;
        try {
            //Creamos el archivo
            File file = new File(path);
            file.createNewFile();

            // Se genera el UUID
            id = UUID.randomUUID().toString();

            // Inicializamos el impresor
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.print(id);
            printWriter.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return id;

    }

    private static String loadIdentityFromFile(String identityFilePath) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(identityFilePath);
        Scanner scanner = new Scanner(fileInputStream);

        String id;
        if (scanner.hasNext()) {
            id = scanner.nextLine();
            try {
                UUID.fromString(id);
            } catch (Exception ex) {
                throw new Exception("Invalid identity structure");
            }

        } else
            throw new Exception("Invalid identity file");

        return id;
    }

    private static Properties loadConfigurations(String filePath) throws IOException {

        System.out.println(filePath);
        Properties currentProperties = new Properties();
        currentProperties.load(new FileReader(filePath));
        return currentProperties;

    }

    private static Properties loadConfigurations() throws IOException {

        Properties currentProperties = new Properties();
        currentProperties.load(new StringReader(System.getenv().toString().replaceAll(",","\n").replaceAll("\\{","").replaceAll("}","")));
        return currentProperties;

    }

    private static String composeMessage(String identity, List<Map<String, String>> dataList) {
        // Get the current TimeStamp
        //String currentTimestamp = getTime();

        // Creamos un json array a partir de los lista de los mapas datos
        //String jsonDataArray = getJsonDataArray(dataList);

        //Componemos el JsonFinal
        JSONObject jsonObject = new JSONObject();
        List<JSONObject> jsonDataList = new LinkedList<>();

        for (Map<String, String> dataElement : dataList)
            jsonDataList.add(new JSONObject(dataElement));

        try {
            jsonObject.put("id", identity);
            jsonObject.put("date", LocalDate.from(getTime()));
            jsonObject.put("timestamp", getTime());
            jsonObject.put("data", jsonDataList);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return jsonObject.toString();
    }

    private static OffsetDateTime getTime() {
        return OffsetDateTime.now();
    }

    private static String getJsonDataArray(List<Map<String, String>> dataList) {
        ObjectMapper mapToJsonMapper = new ObjectMapper();

        StringBuilder dataArray = new StringBuilder();

        for (Map<String, String> data : dataList) {
            try {
                dataArray.append(mapToJsonMapper.writeValueAsString(data)).append(",");
            } catch (Exception ignored) {
            }
        }
        if (dataArray.length() != 0)
            return "[" + dataArray.substring(0, dataArray.length() - 1) + "]";
        else
            return "[]";
    }

}