package factories;

import producers.APCDataProducer.HellaAPCECORS485DataProducer;
import producers.DataProducer;
import producers.GPSDataProducer.Dell3003ArcGISGPSDataProducer;
import producers.GPSDataProducer.Dell3003MarineApiGPSDataProducer;
import producers.GPSDataProducer.Dell3003RxtxGPSDataProducer;
import utils.DataBus;
import utils.SyncronizedDataBus;

public class DataProducerFactory {

    private static DataProducer createDell3003ArcGISGPSDataProducer(){return new Dell3003ArcGISGPSDataProducer("/dev/ttyS0");}

    private static DataProducer createDell3003MarineApiGPSConnector(){return new Dell3003MarineApiGPSDataProducer();}

    private static DataProducer createDell3003rxtxGPSDataProducer(DataBus dataBus){return new Dell3003RxtxGPSDataProducer(dataBus);}

    private static DataProducer createHellaAPCProducer(int doorId, String ipAddress, int port){return new HellaAPCECORS485DataProducer(doorId, ipAddress, port); }

    public static DataProducer create(DataProducerType dataProducerType) throws Exception {
        switch (dataProducerType){
            case Hella_APC_ECO_RS485:
                System.out.println("Hella APC-ECO-RS485 Connector was created using default settings: doorId: 1, ipAddress: 10.42.1.221, port: 10076"); //TODO Create another log console and enhance this pattern to avoid to have lots of creates methods.
                return createHellaAPCProducer(1, "10.42.1.221", 10076);
            case DELL_3003_ARCGIS:
                return createDell3003ArcGISGPSDataProducer();
            case DELL_3003_RXTX:
                return createDell3003rxtxGPSDataProducer(new SyncronizedDataBus());
            default:
                throw new Exception("APCConnector is not well specified.");
        }
    }

    public static DataProducer create(DataProducerType dataProducerType, DataBus dataBus) throws Exception {
        switch (dataProducerType){
            case Hella_APC_ECO_RS485:
                System.out.println("Hella APC-ECO-RS485 Connector was created using default settings: doorId: 1, ipAddress: 10.42.1.221, port: 10076"); //TODO Create another log console and enhance this pattern to avoid to have lots of creates methods.
                return createHellaAPCProducer(1, "10.42.1.221", 10076);
            case DELL_3003_ARCGIS:
                return createDell3003ArcGISGPSDataProducer();
            case DELL_3003_RXTX:
                return createDell3003rxtxGPSDataProducer(dataBus);
            default:
                throw new Exception("APCConnector is not well specified.");
        }
    }
}
