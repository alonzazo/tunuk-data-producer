package factories;

import drivers.APCDataDriver.HellaAPCECORS485DataDriver;
import drivers.DataDriver;
import drivers.GPSDataDriver.Dell3003GPSDataDriver;

public class ConnectorFactory {

    private static DataDriver createDell3003GPSConnector(){return new Dell3003GPSDataDriver();}

    private static DataDriver createHellaAPCConnector(int doorId, String ipAddress, int port){return new HellaAPCECORS485DataDriver(doorId, ipAddress, port); }

    public static DataDriver create(ConnectorType pcsConnectorType, int doorId, String ipAddress, int port) throws Exception {
        switch (pcsConnectorType){
            case Hella_APC_ECO_RS485:
                return createHellaAPCConnector(doorId, ipAddress, port);
            case DELL_3003:
                System.out.println("DELL 3003 Connector was created using useless parameters.");  //TODO Create another log console and enhance this pattern to avoid to have lots of creates methods.
                return createDell3003GPSConnector();
            default:
                throw new Exception("APCConnector is not well specified.");
        }
    }

    public static DataDriver create(ConnectorType pcsConnectorType) throws Exception {
        switch (pcsConnectorType){
            case Hella_APC_ECO_RS485:
                System.out.println("Hella APC-ECO-RS485 Connector was created using default settings: doorId: 1, ipAddress: 10.42.1.221, port: 10076"); //TODO Create another log console and enhance this pattern to avoid to have lots of creates methods.
                return createHellaAPCConnector(1, "10.42.1.221", 10076);
            case DELL_3003:
                return createDell3003GPSConnector();
            default:
                throw new Exception("APCConnector is not well specified.");
        }
    }
}
