package producers.GPSDataProducer;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.sentence.GLLSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.util.Position;
import producers.DataProducer;
import utils.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class HongdianH8922SGPSDataProducer implements DataProducer, Runnable {

    private int port;
    private EventBus EventBus;

    private static class DataProducerIdentity {
        String brand = "Hongdian",
                model= "H8922S",
                serial="",
                dataScheme="GPS",
                controllerVersion="1.0";
    }
    private DataProducerIdentity identity = new DataProducerIdentity();

    public HongdianH8922SGPSDataProducer(int port, EventBus eventBus) {
        this.EventBus = eventBus;
        this.port = port;
    }

    @Override
    public void setBrand(String brandName) {
        identity.brand=brandName;
    }

    @Override
    public String getBrand() {
        return identity.brand;
    }

    @Override
    public void setModel(String modelName) {
        identity.model = modelName;
    }

    @Override
    public String getModel() {
        return identity.model;
    }

    @Override
    public void setSerial(String serial) {
        identity.serial = serial;
    }

    @Override
    public String getSerial() {
        return identity.serial;
    }

    @Override
    public void setDataScheme(String dataScheme) {
        identity.dataScheme = dataScheme;
    }

    @Override
    public String getDataScheme() {
        return identity.dataScheme;
    }

    @Override
    public void setControllerVersion(String controllerVersion) {
        identity.controllerVersion = controllerVersion;
    }

    @Override
    public String getControllerVersion() {
        return identity.controllerVersion;
    }

    @Override
    public EventBus getEventBus() {
        return EventBus;
    }

    @Override
    public void setEventBus(EventBus EventBus) {
        this.EventBus = EventBus;
    }

    @Override
    public void startProduction() throws Exception {

        Thread thread = new Thread(this);
        thread.start();

    }

    @Override
    public void run() {
        while (true){
            try {
                String incomingString = receiveGPSData();


                Map<String,String> gpsData = new HashMap<>();

                //Componemos los datos del GPS
                System.out.println("***********************INTERPRETACION COMENZADA**********************");

                Map<String, String> lineParsed = parseNMEASentence(incomingString);
                if (lineParsed.size() != 0){
                    gpsData.putAll(lineParsed);
                    System.out.println("-----------------------------------INTERPRETACION CORRECTA\nLÍNEA CRUDA GPS: \n" + incomingString + "\nLÍNEA INTERPRETADA GPS:\n" + lineParsed.toString() + "\n----------------------------------");
                }

                if (gpsData.size() != 0){
                    //Agregamos la identidad
                    putIdentityToData(gpsData);
                    // Publicamos los datos en el bus de datos
                    EventBus.publishData(this.getClass(), gpsData);
                } else {
                    System.out.println("No se agregó datos GPS al EventBus");
                }
                System.out.println("******************INTERPRETACION FINALIZADA**************************");
            }

            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            try {
                Thread.sleep(1000);
            } catch (Exception ignored){ }
        }
    }

    private String receiveGPSData() throws IOException {
        DatagramSocket socket = new DatagramSocket(this.port);
        socket.setBroadcast(true);

        DatagramPacket receivingData = new DatagramPacket(new byte[100],100);

        socket.receive(receivingData);

        socket.close();

        byte[] data = new byte[receivingData.getLength()];

        System.arraycopy(receivingData.getData(), receivingData.getOffset(), data, 0, receivingData.getLength());

        String messageReceived = new String(data);

        System.out.println("Mensaje recibido del H8922S:\n" + messageReceived);

        return messageReceived;
    }

    private void putIdentityToData(Map<String,String> data){
        data.put("brand", identity.brand);
        data.put("model", identity.model);
        data.put("serial", identity.serial);
        data.put("data-scheme",identity.dataScheme);
        data.put("controller-version", identity.controllerVersion);
    }

    public Map<String,String> parseNMEASentence(String line) {
        Map<String,String> result = new HashMap<>();
        SentenceFactory sentenceFactory = SentenceFactory.getInstance();


        try {
            Sentence sentence = sentenceFactory.createParser(line);

            if (sentence instanceof GGASentence){
                System.out.println(line + " -> GGA ENCONTRADA");
                GGASentence ggaSentence = (GGASentence) sentence;

                Position position = ggaSentence.getPosition();

                result.put("latitude", String.valueOf(position.getLatitude()));
                result.put("longitude", String.valueOf(position.getLongitude()));
                result.put("altitude", String.valueOf(position.getAltitude()));
            } else if (sentence instanceof GLLSentence){
                System.out.println(line + " -> GLL ENCONTRADA");
                GLLSentence gllSentence = (GLLSentence) sentence;
                Position position = gllSentence.getPosition();

                result.put("latitude", String.valueOf(position.getLatitude()));
                result.put("longitude", String.valueOf(position.getLongitude()));
                result.put("altitude", String.valueOf(position.getAltitude()));
            } else if (sentence instanceof RMCSentence){
                System.out.println(line + " -> RMC ENCONTRADA");
                RMCSentence rmcSentence = (RMCSentence) sentence;
                Position position = rmcSentence.getPosition();

                try {
                    result.put("status", String.valueOf(rmcSentence.getStatus().toChar()));
                } catch (Exception ignored){}
                try {
                    result.put("speed",String.valueOf(rmcSentence.getSpeed()));
                } catch (Exception ignored){}
                try {
                    result.put("trackAngle", String.valueOf(rmcSentence.getCourse()));
                } catch (Exception ignored){}
                try {
                    result.put("magneticVariation", String.valueOf(rmcSentence.getVariation()));
                } catch (Exception ignored){}
                try {
                    result.put("latitude", String.valueOf(position.getLatitude()));
                } catch (Exception ignored){}
                try {
                    result.put("longitude", String.valueOf(position.getLongitude()));
                } catch (Exception ignored){}
                try {
                    result.put("altitude", String.valueOf(position.getAltitude()));
                } catch (Exception ignored){}
            }
            else {
                System.out.println(line + " -> FORMATO NO IDENTIFICADO");
            }

        } catch (Exception ignored){
            ignored.printStackTrace();
            System.out.println(ignored.getMessage());
        }
        return result;
    }
}
