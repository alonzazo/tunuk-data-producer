package drivers.GPSConnector;

import com.esri.core.gps.*;
import com.esri.map.GPSLayer;

import java.util.Map;


public class Dell3003GPSConnector implements GPSConnector {
    @Override
    public Map<String, String> getCurrentData() throws Exception{

        // create SerialPortInfo for a port named "COM2"
        SerialPortInfo myPortInfo = new SerialPortInfo(
                "/dev/ttyS0", BaudRate.BAUD_4800, Parity.NONE, StopBits.ONE, 7);
        SerialPortGPSWatcher myWatcher = new SerialPortGPSWatcher(myPortInfo);
        myWatcher.addListener(new GPSEventListener() {
            @Override
            public void onStatusChanged(GPSStatus gpsStatus) {

            }

            @Override
            public void onPositionChanged(GeoPosition geoPosition) {
                System.out.println(geoPosition.getLocation().toString());
            }

            @Override
            public void onNMEASentenceReceived(String s) {

            }

            @Override
            public void onSatellitesInViewChanged(Map<Integer, Satellite> map) {

            }
        });
        myWatcher.start();
        GPSLayer gpsLayer = new GPSLayer(myWatcher);

        return null;
    }
}
