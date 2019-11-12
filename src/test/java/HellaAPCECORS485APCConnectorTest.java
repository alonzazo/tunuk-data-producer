import producers.APCDataProducer.HellaAPCECORS485DataProducer;
import producers.DataProducer;

public class HellaAPCECORS485APCConnectorTest {

    public static void main(String args[]) throws Exception {

        int doorId = Integer.parseInt(args[0]);
        String ipAddress = args[1];
        int port = Integer.parseInt(args[2]);

        DataProducer dataProducerHella = new HellaAPCECORS485DataProducer(doorId,ipAddress,port);
        dataProducerHella.startProduction();

    }
}
