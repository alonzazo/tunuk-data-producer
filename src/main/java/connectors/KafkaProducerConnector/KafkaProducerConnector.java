package connectors.KafkaProducerConnector;

import connectors.IoTConnector;
import connectors.IoTConnectorException;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.time.Instant;
import java.util.Properties;


public class KafkaProducerConnector implements IoTConnector {

    static Logger log = LoggerFactory.getLogger(KafkaProducerConnector.class);

    private Producer<String, String> kafkaProducer;
    private String bootstrapServersConfig;
    private String keySerializerClassConfig = "org.apache.kafka.common.serialization.ByteArraySerializer";
    private String valueSerializerClassConfig = "org.apache.kafka.common.serialization.StringSerializer";

    public KafkaProducerConnector(){
        bootstrapServersConfig = "localhost:9092";
    }

    public KafkaProducerConnector(String bootstrapServers){
        this.bootstrapServersConfig = bootstrapServers;
    }

    @Override
    public void connect() throws IoTConnectorException {
        Properties configProperties = new Properties();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerClassConfig);
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,valueSerializerClassConfig);
        /*configProperties.put(ProducerConfig.RETRIES_CONFIG, 1000);
        configProperties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        configProperties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 750);
        configProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 500);*/
        configProperties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10000);
        kafkaProducer = new KafkaProducer<>(configProperties);
    }

    @Override
    public void reset() throws IoTConnectorException {
        kafkaProducer.close();
        connect();
    }

    @Override
    public void configure(String propertiesPath) throws IoTConnectorException {
        try {
            FileInputStream fileInputStream = new FileInputStream(propertiesPath);

            Properties properties = new Properties();
            properties.load(fileInputStream);

            bootstrapServersConfig = properties.get("bootstrapServersConfig").toString();
            keySerializerClassConfig = properties.getProperty("keySerializerClassConfig");
            valueSerializerClassConfig = properties.getProperty("valueSerializerClassConfig");

            connect();

        }catch (Exception e){
            throw new IoTConnectorException(e);
        }
    }

    @Override
    public void publish(String topic, String message) throws IoTConnectorException {
        //Producer<String, String> kafkaProducer = null;
        try {

            /*Properties configProperties = new Properties();
            configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);
            configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerClassConfig);
            configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,valueSerializerClassConfig);
        *//*configProperties.put(ProducerConfig.RETRIES_CONFIG, 1000);
        configProperties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        configProperties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 750);
        configProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 500);*//*
            configProperties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10000);

            kafkaProducer = new KafkaProducer<>(configProperties);*/


            ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
            RecordMetadata resultMetadata = this.kafkaProducer.send(record).get();

            /*kafkaProducer.close();*/


            /*log.info(Instant.now() + " " + Thread.currentThread().getName() + " [KAFKA_RESULT]: Message was received successfully: " + message);*/
            log.info(Instant.now() + " " + Thread.currentThread().getName() + " [KAFKA_RESULT]: Message was received successfully: " + message);

        } catch (Exception e) {
/*            if (kafkaProducer != null)
                kafkaProducer.close();*/
            throw new IoTConnectorException(e);
        }


    }

    @Override
    public void close() throws IoTConnectorException {
        kafkaProducer.close();
    }
}
