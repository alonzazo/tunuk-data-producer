package factories;

import drivers.APCConnector.HellaAPCECORS485APCConnector;
import drivers.APCConnector.APCConnector;

public class APCConnectorFactory {

    private static APCConnector createHellaAPCConnector(int doorId, String ipAddress, int port){return new HellaAPCECORS485APCConnector(doorId, ipAddress, port); }

    public static APCConnector create(APCConnectorType pcsConnectorType, int doorId, String ipAddress, int port) throws Exception {
        switch (pcsConnectorType){
            case Hella_APC_ECO_RS485:
                return createHellaAPCConnector(doorId, ipAddress, port);
            default:
                throw new Exception("APCConnector is not well specified.");
        }
    }
}
