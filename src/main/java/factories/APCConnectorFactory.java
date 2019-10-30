package factories;

import drivers.APCConnector.HellaAPCECORS485APCConnector;
import drivers.APCConnector.APCConnector;

public class APCConnectorFactory {

    private static APCConnector createHellaAPCConnector(){return new HellaAPCECORS485APCConnector(); }

    public static APCConnector create(APCConnectorType pcsConnectorType) throws Exception {
        switch (pcsConnectorType){
            case Hella_APC_ECO_RS485:
                return createHellaAPCConnector();
            default:
                throw new Exception("APCConnector is not well specified.");
        }
    }
}
