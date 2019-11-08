package factories;

import EventListener.EventListener;
import drivers.APCDataDriver.HellaAPCECORS485DataDriver;
import drivers.DataDriver;
import drivers.GPSDataDriver.Dell3003ArcGISGPSDataDriver;
import drivers.GPSDataDriver.Dell3003MarineApiGPSDataDriver;
import drivers.GPSDataDriver.Dell3003rxtxGPSDataDriver;

import javax.xml.crypto.Data;

public class ConnectorFactory {

    private static DataDriver createDell3003ArcGISGPSConnector(){return new Dell3003ArcGISGPSDataDriver("/dev/ttyS0");}

    private static DataDriver createDell3003ArcGISGPSConnector(String portName){return new Dell3003ArcGISGPSDataDriver(portName);}

    private static DataDriver createDell3003MarineApiGPSConnector(){return new Dell3003MarineApiGPSDataDriver();}

    private static DataDriver createDell3003rxtxGPSConnector(EventListener eventListener){return new Dell3003rxtxGPSDataDriver(eventListener);}

    private static DataDriver createHellaAPCConnector(int doorId, String ipAddress, int port){return new HellaAPCECORS485DataDriver(doorId, ipAddress, port); }

    public static DataDriver create(ConnectorType pcsConnectorType, String portName) throws Exception {
        switch (pcsConnectorType){
            case Hella_APC_ECO_RS485:
                System.out.println("Hella APC-ECO-RS485 Connector was created using default settings: doorId: 1, ipAddress: 10.42.1.221, port: 10076"); //TODO Create another log console and enhance this pattern to avoid to have lots of creates methods.
                return createHellaAPCConnector(1, "10.42.1.221", 10076);
            case DELL_3003_ARCGIS:
                return createDell3003ArcGISGPSConnector(portName);
            default:
                throw new Exception("APCConnector is not well specified.");
        }
    }

    public static DataDriver create(ConnectorType pcsConnectorType, int doorId, String ipAddress, int port) throws Exception {
        switch (pcsConnectorType){
            case Hella_APC_ECO_RS485:
                return createHellaAPCConnector(doorId, ipAddress, port);
            case DELL_3003_ARCGIS:
                System.out.println("DELL 3003 Connector was created using useless parameters.");  //TODO Create another log console and enhance this pattern to avoid to have lots of creates methods.
                return createDell3003ArcGISGPSConnector();
            case DELL_3003_MARINE_API:
                return createDell3003MarineApiGPSConnector();
            case DELL_3003_RXTX:
                return createDell3003rxtxGPSConnector(System.out::println);
            default:
                throw new Exception("APCConnector is not well specified.");
        }
    }

    public static DataDriver create(ConnectorType pcsConnectorType) throws Exception {
        switch (pcsConnectorType){
            case Hella_APC_ECO_RS485:
                System.out.println("Hella APC-ECO-RS485 Connector was created using default settings: doorId: 1, ipAddress: 10.42.1.221, port: 10076"); //TODO Create another log console and enhance this pattern to avoid to have lots of creates methods.
                return createHellaAPCConnector(1, "10.42.1.221", 10076);
            case DELL_3003_ARCGIS:
                return createDell3003ArcGISGPSConnector();
            case DELL_3003_MARINE_API:
                return createDell3003MarineApiGPSConnector();
            case DELL_3003_RXTX:
                return createDell3003rxtxGPSConnector( (data)-> {
                    System.out.println(data.toString());
                });
            default:
                throw new Exception("APCConnector is not well specified.");
        }
    }

    public  static DataDriver create(ConnectorType pcsConnectorType, EventListener eventListener) throws Exception {
        switch (pcsConnectorType){
            case Hella_APC_ECO_RS485:
                System.out.println("Hella APC-ECO-RS485 Connector was created using default settings: doorId: 1, ipAddress: 10.42.1.221, port: 10076"); //TODO Create another log console and enhance this pattern to avoid to have lots of creates methods.
                return createHellaAPCConnector(1, "10.42.1.221", 10076);
            case DELL_3003_ARCGIS:
                return createDell3003ArcGISGPSConnector();
            case DELL_3003_MARINE_API:
                return createDell3003MarineApiGPSConnector();
            case DELL_3003_RXTX:
                return createDell3003rxtxGPSConnector(eventListener);
            default:
                throw new Exception("APCConnector is not well specified.");
        }
    }
}
