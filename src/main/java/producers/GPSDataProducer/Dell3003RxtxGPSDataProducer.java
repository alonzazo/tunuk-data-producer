package producers.GPSDataProducer;

import producers.DataProducer;


import jssc.SerialPort; import jssc.SerialPortEvent; import jssc.SerialPortEventListener; import jssc.SerialPortException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.util.Position;
import utils.DataBus;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Dell3003RxtxGPSDataProducer implements DataProducer, SerialPortEventListener {


    private SerialPort serialPort;
    private String residualStream = "";
    private DataBus dataBus;

    private static class DataProducerIdentity {
        String brand = "Dell",
                model= "3003",
                serial="",
                dataScheme="GPS",
                controllerVersion="1.0";
    }
    private DataProducerIdentity identity = new DataProducerIdentity();

    public Dell3003RxtxGPSDataProducer(DataBus dataBus) {
        this.dataBus = dataBus;
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
    public DataBus getDataBus() {
        return dataBus;
    }

    @Override
    public void setDataBus(DataBus dataBus) {
        this.dataBus = dataBus;
    }

    @Override
    public void startProduction() throws Exception {

        serialPort = new SerialPort("/dev/ttyHS0");
        System.out.println("Port opened: " + serialPort.openPort());
        System.out.println("Params setted: " + serialPort.setParams(9600, 8, 1, 0));//Set params.
        serialPort.addEventListener(this);//Add SerialPortEventListener

    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        try {
            byte[] bytes = serialPort.readBytes();

            if (bytes != null){
                String incomingString = new String(bytes,StandardCharsets.UTF_8);

                if (incomingString.charAt(0) == '$')
                    residualStream += "\r\n";

                residualStream += incomingString;

                String[] lines = residualStream.split("\r\n");

                residualStream = lines[lines.length-1];

                Map<String,String> gpsData = new HashMap<>();

                //Agregamos la identidad
                putIdentityToData(gpsData);

                //Componemos los datos del GPS

                for (int i = 0; i < lines.length - 1; i++){
                    /*gpsData.putAll(parseNMEASentence("$GPGGA,224900.000,4832.3762,N,00903.5393,E,1,04,7.8,498.6,M,48.0,M,,0000*5E"));*/
                    gpsData.putAll(parseNMEASentence(lines[i]));
                    System.out.println(lines[i]);
                }
                // Publicamos los datos en el bus de datos
                dataBus.publishData(this.getClass(), gpsData);
            }
        }

        catch (SerialPortException ex) {
            System.out.println(ex.getMessage());
        }
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
        try {
            SentenceFactory sentenceFactory = SentenceFactory.getInstance();
            GGASentence sentence = (GGASentence) sentenceFactory.createParser(line);
            Position position = sentence.getPosition();

            result.put("latitude", String.valueOf(position.getLatitude()));
            result.put("longitude", String.valueOf(position.getLongitude()));
            result.put("altitude", String.valueOf(position.getAltitude()));
        } catch (Exception ex){

        }
        return result;
    }
}
