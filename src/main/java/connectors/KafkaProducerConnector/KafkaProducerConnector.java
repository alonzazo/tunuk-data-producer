package connectors.KafkaProducerConnector;

import connectors.IoTConnector;
import connectors.IoTConnectorException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileInputStream;
import java.util.Properties;


public class KafkaProducerConnector implements IoTConnector {

    private Producer kafkaProducer;
    private String bootstrapServersConfig = "18.217.74.235:9092";
    private String keySerializerClassConfig = "org.apache.kafka.common.serialization.ByteArraySerializer";
    private String valueSerializerClassConfig = "org.apache.kafka.common.serialization.StringSerializer";

    public KafkaProducerConnector(){
    }

    @Override
    public void connect() throws IoTConnectorException {
        Properties configProperties = new Properties();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerClassConfig);
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,valueSerializerClassConfig);
        kafkaProducer = new KafkaProducer(configProperties);
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
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
        kafkaProducer.send(record);

    }

    @Override
    public void close() throws IoTConnectorException {
        kafkaProducer.close();
    }
}
