package factories;

import drivers.GPSConnector.Dell3003GPSConnector;
import drivers.GPSConnector.GPSConnector;

public class GPSConnectorFactory {
    private static GPSConnector createDell3003GPSConnector(){return new Dell3003GPSConnector();}

    public static GPSConnector create(GPSConnectorType gpsConnectorType) throws Exception {
        switch (gpsConnectorType){
            case DELL_3003:
                return createDell3003GPSConnector();
            default:
                throw new Exception("GPSConnectorType is not well specified.");
        }
    }

}
