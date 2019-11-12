package producers.GPSDataProducer;

import com.esri.core.gps.*;
import producers.DataProducer;
import utils.DataBus;

import java.util.Map;


public class Dell3003ArcGISGPSDataProducer implements DataProducer {

    private String portName;

    public Dell3003ArcGISGPSDataProducer(String portName){
        this.portName = portName;
    }
    @Override
    public void startProduction() throws Exception{

        // create SerialPortInfo for a port named "COM2"
        SerialPortInfo myPortInfo = new SerialPortInfo(
                portName, BaudRate.BAUD_9600, Parity.NONE, StopBits.ONE, 7);
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
    }

    @Override
    public DataBus getDataBus() {
        return null;
    }

    @Override
    public void setDataBus(DataBus dataBus) {

    }

    @Override
    public void setBrand(String brandName) {

    }

    @Override
    public String getBrand() {
        return null;
    }

    @Override
    public void setModel(String modelName) {

    }

    @Override
    public String getModel() {
        return null;
    }

    @Override
    public void setSerial(String serial) {

    }

    @Override
    public String getSerial() {
        return null;
    }

    @Override
    public void setDataScheme(String dataScheme) {

    }

    @Override
    public String getDataScheme() {
        return null;
    }

    @Override
    public void setControllerVersion(String controllerVersion) {

    }

    @Override
    public String getControllerVersion() {
        return null;
    }
}
