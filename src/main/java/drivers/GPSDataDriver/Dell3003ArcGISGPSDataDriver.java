package drivers.GPSDataDriver;

import com.esri.core.gps.*;
import com.esri.map.GPSLayer;
import drivers.DataDriver;

import java.util.Map;


public class Dell3003ArcGISGPSDataDriver implements DataDriver {

    private String portName;

    public Dell3003ArcGISGPSDataDriver(String portName){
        this.portName = portName;
    }
    @Override
    public Map<String, String> getData() throws Exception{

        // create SerialPortInfo for a port named "COM2"
        SerialPortInfo myPortInfo = new SerialPortInfo(
                portName, BaudRate.BAUD_4800, Parity.NONE, StopBits.ONE, 7);
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
