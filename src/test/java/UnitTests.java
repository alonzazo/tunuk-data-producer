import producers.DataProducer;
import producers.GPSDataProducer.Dell3003RxtxGPSDataProducer;
import producers.GPSDataProducer.HongdianH8922SGPSDataProducer;
import utils.SyncronizedDataBus;

public class UnitTests {

    private static void dell3003rxtxGPSDataDriver() throws Exception {
        Dell3003RxtxGPSDataProducer rxtxGPSDataDriver = new Dell3003RxtxGPSDataProducer(new SyncronizedDataBus());
        rxtxGPSDataDriver.startProduction();
    }

    private static void parseSentence(){

        Dell3003RxtxGPSDataProducer rxtxGPSDataDriver = new Dell3003RxtxGPSDataProducer(new SyncronizedDataBus());

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

    private static void H8922STest() throws Exception {

        DataProducer gpsData = new HongdianH8922SGPSDataProducer(2502, new SyncronizedDataBus());
        gpsData.startProduction();

    }

    public static void main(String args[]){
        try {

            /*dell3003rxtxGPSDataDriver();*/

            /*parseSentence();*/

            H8922STest();

            /*List<Map<String,String>> list = new LinkedList<>();

            list.add(new HashMap<>());
            list.add(new HashMap<>());

            list.clear();

            list.add(new HashMap<>());
            list.add(new HashMap<>());*/
/*
            list.clear();*/


        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
