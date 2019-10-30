package drivers.GPSConnector;

import java.util.Map;

public interface GPSConnector {
    Map<String,String> getCurrentData() throws Exception;
}
