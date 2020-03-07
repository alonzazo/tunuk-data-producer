package producers.GPSDataProducer;

import eventbuses.EventBus;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.sentence.GLLSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.util.Position;
import producers.DataProducer;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AdafruitUltimateGPSDataProducer implements DataProducer, SerialPortEventListener {

    ///--------------------------------------------------------------------PROPIEDADES o ATRIBUTOS
    private SerialPort serialPort;
    private String serialPortName;
    private String residualStream = "";
    private EventBus EventBus;

    private static class DataProducerIdentity {
        String brand = "Adafruit",
                model= "Flora/UltimateGPS",
                serial="",
                dataScheme="GPS",
                controllerVersion="1.0";
    }
    private DataProducerIdentity identity = new DataProducerIdentity();

    public AdafruitUltimateGPSDataProducer(String serialPortName, EventBus EventBus) {
        this.EventBus = EventBus; this.serialPortName = serialPortName;
    }

    ///-----------------------------------------------------------METODOS

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

        serialPort = new SerialPort(serialPortName);
        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + "Port opened: " + serialPort.openPort());
        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + "Params setted: " + serialPort.setParams(9600, 8, 1, 0));//Set params.
        serialPort.addEventListener(this);//Add SerialPortEventListener

    }

    private StringBuilder message = new StringBuilder();
    private Boolean receivingMessage = false;

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if(serialPortEvent.isRXCHAR() && serialPortEvent.getEventValue() > 0){
            try {
                byte buffer[] = serialPort.readBytes();
                for (byte b: buffer) {
                    if (b == '$') {
                        receivingMessage = true;
                        message.setLength(0);
                        message.append((char)b);
                    }
                    else if (receivingMessage) {
                        if (b == '\r') {
                            receivingMessage = false;
                            String toProcess = message.toString();

                            Map<String, String> lineParsed = parseNMEASentence(toProcess);
                            if (lineParsed.size() != 0){

                                //Agregamos la identidad
                                putIdentityToData(lineParsed);
                                // Publicamos los datos en el bus de datos
                                EventBus.publishData(this.getClass(), lineParsed);

                                System.out.println(Instant.now().toString() + " [INTERPRETACION CORRECTA]: \n↳CRUDA:        " + toProcess + "\n↳INTERPRETADA: " + lineParsed.toString());
                            }

                        }
                        else {
                            message.append((char)b);
                        }
                    }
                }
            }
            catch (SerialPortException ex) {
                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " " + ex);
            }
        }
        /*try {
            byte[] bytes = serialPort.readBytes();

            if (bytes != null){
                String incomingString = new String(bytes,StandardCharsets.UTF_8);

                System.out.println("ENTRADA DEL GPS CRUDA:\n" + incomingString);

                if (incomingString.charAt(0) == '$')
                    residualStream += "\r\n";

                residualStream += incomingString;

                String[] lines = residualStream.split("\r\n");

                *//*String[] lines = incomingString.split("\r\n");*//*

                residualStream = lines[lines.length-1];

                Map<String,String> gpsData = new HashMap<>();

                //Componemos los datos del GPS

                System.out.println("***********************INTERPRETACION COMENZADA**********************");
                for (int i = 0; i < lines.length - 1; i++){
                    //gpsData.putAll(parseNMEASentence("$GPGGA,224900.000,4832.3762,N,00903.5393,E,1,04,7.8,498.6,M,48.0,M,,0000*5E"));
                    Map<String, String> lineParsed = parseNMEASentence(lines[i]);
                    if (lineParsed.size() != 0){
                        gpsData.putAll(lineParsed);
                        System.out.println("-----------------------------------INTERPRETACION CORRECTA\nLÍNEA CRUDA GPS: \n" + lines[i] + "\nLÍNEA INTERPRETADA GPS:\n" + lineParsed.toString() + "\n----------------------------------");
                    }
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
        }

        catch (SerialPortException ex) {
            System.out.println(ex.getMessage());
        }*/

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
                System.out.println(Instant.now().toString() + " [ GGA ENCONTRADA ]:          "+ line);
                GGASentence ggaSentence = (GGASentence) sentence;

                Position position = ggaSentence.getPosition();

                result.put("latitude", String.valueOf(position.getLatitude()));
                result.put("longitude", String.valueOf(position.getLongitude()));
                result.put("altitude", String.valueOf(position.getAltitude()));
            } else if (sentence instanceof RMCSentence){
                System.out.println(Instant.now().toString() + " [ RMC ENCONTRADA ]:          " + line);
                RMCSentence rmcSentence = (RMCSentence) sentence;
                Position position = rmcSentence.getPosition();

                result.put("latitude", String.valueOf(position.getLatitude()));
                result.put("longitude", String.valueOf(position.getLongitude()));
                result.put("altitude", String.valueOf(position.getAltitude()));
            }
            else if (sentence instanceof GLLSentence){
                System.out.println(Instant.now().toString() + " [ GLL ENCONTRADA ]:          " + line);
                GLLSentence gllSentence = (GLLSentence) sentence;
                Position position = gllSentence.getPosition();

                result.put("latitude", String.valueOf(position.getLatitude()));
                result.put("longitude", String.valueOf(position.getLongitude()));
                result.put("altitude", String.valueOf(position.getAltitude()));
            } else {
                System.out.println(Instant.now().toString() + " [ FORMATO NO IDENTIFICADO ]: " + line );
            }

        }
        catch (DataNotAvailableException e){
            System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [ EXCEPCION ]:               Datos no disponibles");
        }
        catch (Exception ignored){
            //ignored.printStackTrace();
            System.out.println(ignored.getMessage());
        }
        return result;
    }
}
