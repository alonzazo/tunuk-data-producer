package connectors.AWSIoTConnector;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import connectors.IoTConnector;
import connectors.IoTConnectorException;
import utils.SampleUtil;

import java.io.FileInputStream;
import java.util.Properties;


public class AWSIoTConnector implements IoTConnector {


    private String clientEndpoint;       // replace <prefix> and <region> with your own
    private String clientId;                              // replace with your own client ID. Use unique client IDs for concurrent connections.
    private String certificateFile;                       // X.509 based certificate file
    private String privateKeyFile;                        // PKCS#1 or PKCS#8 PEM encoded private key file
    private AWSIotMqttClient client;

    public AWSIoTConnector(){
        try {
            reset();
        }catch (IoTConnectorException e){
            clientEndpoint = "";
            clientId = "";
            certificateFile = "";
            privateKeyFile = "";
        }
    }

    public void connect() throws IoTConnectorException {
        // SampleUtil.java and its dependency PrivateKeyReader.java can be copied from the sample source code.
// Alternatively, you could load key store directly from a file - see the example included in this README.
        SampleUtil.KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile);
        client = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
        client.setMaxOfflineQueueSize(10);
        try{
            // optional parameters can be set before connect()
            client.connect();
        } catch (AWSIotException e){
            throw new IoTConnectorException(e);
        }

    }

    public void reset() throws IoTConnectorException {
        configure("props.conf");
    }


    public void configure(String propertiesPath) throws IoTConnectorException {
        try {
            FileInputStream fileInputStream = new FileInputStream(propertiesPath);

            Properties properties = new Properties();
            properties.load(fileInputStream);

            clientId = properties.get("clientId").toString();
            clientEndpoint = properties.getProperty("clientEndpoint");
            certificateFile = properties.getProperty("certificateFile");
            privateKeyFile = properties.getProperty("privateKeyFile");
        }catch (Exception e){
            throw new IoTConnectorException(e);
        }


    }

    public void publish(String topic, String message) throws IoTConnectorException {
        class MyMessage extends AWSIotMessage {
            public MyMessage(String topic, AWSIotQos qos, String payload) {
                super(topic, qos, payload);
            }

            @Override
            public void onSuccess() {
                // called when message publishing succeeded
            }

            @Override
            public void onFailure() {
                // called when message publishing failed
            }

            @Override
            public void onTimeout() {
                // called when message publishing timed out
            }
        }

        AWSIotQos qos = AWSIotQos.QOS1;
        long timeout = 3000;                    // milliseconds

        System.out.println("[MENSAJE] Por transmitirse: " + message);
        MyMessage myMessage = new MyMessage(topic, qos, message);

        try {
            client.publish(myMessage, timeout);
            System.out.println("[MENSAJE] Transmitido con éxito");
        } catch (AWSIotException e) {
            throw new IoTConnectorException(e);
        }

    }

    public void close() throws IoTConnectorException {
        try {
            client.disconnect();
        } catch (AWSIotException e) {
            throw new IoTConnectorException(e);
        }
    }
}
