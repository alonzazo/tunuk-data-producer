import eventbuses.StreamingEventBus;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.junit.jupiter.api.Test;
import producers.DataProducer;
import producers.GPSDataProducer.Dell3003RxtxGPSDataProducer;
import producers.GPSDataProducer.HongdianH8922SGPSDataProducer;

import java.io.FileReader;
import java.util.*;

class Pair<T,U> extends AbstractMap.SimpleEntry<T,U> {

    public Pair(T key, U value) {
        super(key, value);
    }

    public Pair(Map.Entry<? extends T, ? extends U> entry) {
        super(entry);
    }
}

public class UnitTests {

    @Test
    private void dell3003rxtxGPSDataDriver() throws Exception {
        Dell3003RxtxGPSDataProducer rxtxGPSDataDriver = new Dell3003RxtxGPSDataProducer("/dev/ttyHS0",new StreamingEventBus());
        rxtxGPSDataDriver.startProduction();
    }

    @Test
    private void parseSentence(){

        Dell3003RxtxGPSDataProducer rxtxGPSDataDriver = new Dell3003RxtxGPSDataProducer("/dev/ttyHS0",new StreamingEventBus());

        String result = "";
        result += rxtxGPSDataDriver.parseNMEASentence("$GNGGA,184208.00,,,,,0,00,99.99,,,,,,*7F") + "\n";
        result += rxtxGPSDataDriver.parseNMEASentence("$GNGSA,A,1,,,,,,,,,,,,,99.99,99.99,99.99*2E") + "\n" ;
        result += rxtxGPSDataDriver.parseNMEASentence("$GNGSA,A,1,,,,,,,,,,,,,99.99,99.99,99.99*2E") +"\n" ;
        result += rxtxGPSDataDriver.parseNMEASentence("$GPGSV,1,1,01,03,,,15*7F") +"\n" ;
        result += rxtxGPSDataDriver.parseNMEASentence("$GLGSV,1,1,02,,,,20,,,,15*61") +"\n" ;
        result += rxtxGPSDataDriver.parseNMEASentence("$GNGLL,,,,,184208.00,V,N*53") +"\n" ;
        result += rxtxGPSDataDriver.parseNMEASentence("$GNGLL,,,,,000715.00,V,N*57290,,08,22,187,,11,01,217,*78");
        result += rxtxGPSDataDriver.parseNMEASentence("$GNRMC,184209.00,V,,,,,,,061119,,,N*6B") +"\n";
        result += rxtxGPSDataDriver.parseNMEASentence("$GNVTG,,,,,,,,,N*2E");
        result += rxtxGPSDataDriver.parseNMEASentence("$GPGGA,224900.000,4832.3762,N,00903.5393,E,1,04,7.8,498.6,M,48.0,M,,0000*5E");



        //System.out.println(result);
    }

    @Test
    private void H8922STest() throws Exception {

        DataProducer gpsData = new HongdianH8922SGPSDataProducer(2502, new StreamingEventBus());
        gpsData.startProduction();

    }

    @Test
    private void configurationProps() throws Exception{
        System.out.println(System.getProperty("user.dir"));
        FileReader fileReader = new FileReader("src/main/resources/configurations/producers-config.properties");

        Scanner scanner = new Scanner(fileReader);
        scanner.useDelimiter("\n");

        List<Pair<String, Properties>> producersList = new LinkedList<>();
        int LINES_LIMIT = 1024;

        Properties currentProperties = new Properties();
        String currentControllerName = "";

        for (int i = 0; scanner.hasNext() && i < LINES_LIMIT; i++){
            String line = scanner.next();

            if (line.length() > 0 && line.trim().charAt(0) == '#'){
                continue;
            }
            else if (line.matches("[^=]+=[^=]*")){
                String[] keyValue = line.split("=");
                currentProperties.put( keyValue[0].trim(), keyValue[1].trim() );

                if (i == LINES_LIMIT - 1 || !scanner.hasNext()){
                    producersList.add(new Pair<>(currentControllerName,currentProperties));
                }
            }
            else if (!line.trim().equals("")){
                if (!currentControllerName.equals("")){
                    producersList.add(new Pair<>(currentControllerName,currentProperties));
                    currentProperties = new Properties();
                }
                currentControllerName = line.trim();
            }
        }

        System.out.println(producersList);
    }

    @Test
    public void testEnvironment(){

        System.out.println(System.getenv());
    }

    @Test
    public void testSerialCANBUS() throws SerialPortException {
        SerialPort serialPort = new SerialPort("/dev/ttyUSB0");
        serialPort.openPort();
        serialPort.setParams(9600, 8, 1, 0);

    }
}
