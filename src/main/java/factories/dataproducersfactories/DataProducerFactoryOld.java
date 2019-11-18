package factories.dataproducersfactories;

import absfactories.DataProducerType;
import producers.APCDataProducer.HellaAPCECORS485DataProducer;
import producers.DataProducer;
import producers.GPSDataProducer.Dell3003ArcGISGPSDataProducer;
import producers.GPSDataProducer.Dell3003MarineApiGPSDataProducer;
import producers.GPSDataProducer.Dell3003RxtxGPSDataProducer;
import producers.GPSDataProducer.HongdianH8922SGPSDataProducer;
import utils.DataBus;
import utils.SyncronizedDataBus;

public class DataProducerFactoryOld {

    private static DataProducer createDell3003ArcGISGPSDataProducer(){return new Dell3003ArcGISGPSDataProducer("/dev/ttyS0");}

    private static DataProducer createDell3003MarineApiGPSConnector(){return new Dell3003MarineApiGPSDataProducer();}

    private static DataProducer createDell3003rxtxGPSDataProducer(DataBus dataBus){return new Dell3003RxtxGPSDataProducer("/dev/ttyHS0", dataBus);}

    private static DataProducer createHongdianH8922SGPSDataProducer(int port, DataBus dataBus){ return new HongdianH8922SGPSDataProducer(port, dataBus); }

    private static DataProducer createHellaAPCProducer(int doorId, String ipAddress, int port, DataBus dataBus){return new HellaAPCECORS485DataProducer(doorId, ipAddress, port, dataBus); }

    public static DataProducer create(DataProducerType dataProducerType) throws Exception {
        switch (dataProducerType){
            case HELLA_APC_ECO_RS485:
                System.out.println("Hella APC-ECO-RS485 Connector was created using default settings: doorId: 1, ipAddress: 10.42.1.221, port: 10076, dataBus: new SyncronizedDataBus"); //TODO Create another log console and enhance this pattern to avoid to have lots of creates methods.
                return createHellaAPCProducer(1, "10.42.1.221", 10076, new SyncronizedDataBus());
            case DELL_3003_ARCGIS:
                return createDell3003ArcGISGPSDataProducer();
            case DELL_3003_RXTX:
                return createDell3003rxtxGPSDataProducer(new SyncronizedDataBus());
            case HONGDIAN_H8922S:
                return createHongdianH8922SGPSDataProducer(2502,new SyncronizedDataBus());
            default:
                throw new Exception("APCConnector is not well specified.");
        }
    }

    public static DataProducer create(DataProducerType dataProducerType, DataBus dataBus) throws Exception {
        switch (dataProducerType){
            case HELLA_APC_ECO_RS485:
                System.out.println("Hella APC-ECO-RS485 Connector was created using default settings: doorId: 1, ipAddress: 10.42.1.221, port: 10076"); //TODO Create another log console and enhance this pattern to avoid to have lots of creates methods.
                return createHellaAPCProducer(1, "10.42.1.221", 10076, new SyncronizedDataBus());
            case DELL_3003_ARCGIS:
                return createDell3003ArcGISGPSDataProducer();
            case DELL_3003_RXTX:
                return createDell3003rxtxGPSDataProducer(dataBus);
            case HONGDIAN_H8922S:
                return createHongdianH8922SGPSDataProducer(2502,dataBus);
            default:
                throw new Exception("APCConnector is not well specified.");
        }
    }

    public static DataProducer create(DataProducerType dataProducerType, DataBus dataBus, int doorId, String ipAddress, int port) throws Exception { //TODO Make an abstract factory to avoid this
        switch (dataProducerType){
            case HELLA_APC_ECO_RS485:
                return createHellaAPCProducer(doorId, ipAddress, port, dataBus);
            case DELL_3003_ARCGIS:
                return createDell3003ArcGISGPSDataProducer();
            case DELL_3003_RXTX:
                return createDell3003rxtxGPSDataProducer(dataBus);
            case HONGDIAN_H8922S:
                createHongdianH8922SGPSDataProducer(port,dataBus);
            default:
                throw new Exception("APCConnector is not well specified.");
        }
    }
}
