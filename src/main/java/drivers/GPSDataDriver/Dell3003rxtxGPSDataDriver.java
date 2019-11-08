package drivers.GPSDataDriver;

import EventListener.EventListener;
import drivers.AWSIoTConnector.AWSIoTConnectorException;
import drivers.DataDriver;


import jssc.SerialPort; import jssc.SerialPortEvent; import jssc.SerialPortEventListener; import jssc.SerialPortException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.GGASentence;
import net.sf.marineapi.nmea.util.Position;
import org.json.JSONException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Dell3003rxtxGPSDataDriver implements DataDriver, SerialPortEventListener {


    private SerialPort serialPort;
    private String residualStream = "";
    private EventListener eventListener;

    public Dell3003rxtxGPSDataDriver(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public Map<String, String> getData() throws Exception {

        serialPort = new SerialPort("/dev/ttyHS0");
        System.out.println("Port opened: " + serialPort.openPort());
        System.out.println("Params setted: " + serialPort.setParams(9600, 8, 1, 0));//Set params.
        serialPort.addEventListener(this);//Add SerialPortEventListener

        return null;
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

                //Componemos los datos del GPS

                for (int i = 0; i < lines.length - 1; i++){
                    gpsData.putAll(parseNMEASentence("$GPGGA,224900.000,4832.3762,N,00903.5393,E,1,04,7.8,498.6,M,48.0,M,,0000*5E"));
                    gpsData.putAll(parseNMEASentence(lines[i]));
                    System.out.println(lines[i]);
                }
                // Corremos el listener que quiera el cliente
                eventListener.listen(gpsData);

                /*// Cerramos conexion
                serialPort.removeEventListener();
                System.out.println("EventListener removed");
                serialPort.closePort();
                System.out.println("Port " + serialPort.getPortName() + " closed");
                */
            }
        }
        catch (SerialPortException ex) {
            System.out.println(ex.getMessage());
        } catch (AWSIoTConnectorException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
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
