package drivers.GPSDataDriver;

import java.util.Map;

public interface RxtxGPSListener {
    void listenGPSEvent(Map<String,String> gpsData);
}
