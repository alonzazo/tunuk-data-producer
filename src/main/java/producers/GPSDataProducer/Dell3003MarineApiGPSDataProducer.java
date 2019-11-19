package producers.GPSDataProducer;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.SentenceValidator;
import producers.DataProducer;
import eventbuses.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Dell3003MarineApiGPSDataProducer implements DataProducer, SentenceListener {
    @Override
    public EventBus getEventBus() {
        return null;
    }

    @Override
    public void setEventBus(EventBus EventBus) {

    }

    @Override
    public void readingPaused() {
        System.out.println("-- Paused --");
    }

    @Override
    public void readingStarted() {
        System.out.println("-- Started --");
    }

    @Override
    public void readingStopped() {
        System.out.println("-- Stopped --");

    }

    @Override
    public void sentenceRead(SentenceEvent sentenceEvent) {
        // here we receive each sentence read from the port
        System.out.println(sentenceEvent.getSentence());
    }

    @Override
    public void startProduction() throws Exception {
        init();
    }

    /**
     * Scan serial ports for NMEA data.
     *
     * @return SerialPort from which NMEA data was found, or null if data was
     *         not found in any of the ports.
     */
    private SerialPort getSerialPort() {
        try {
            //Enumeration<?> e = CommPortIdentifier.getPortIdentifier("/dev/ttyHS0");

            //while (e.hasMoreElements()) {
                CommPortIdentifier id = CommPortIdentifier.getPortIdentifier("/dev/ttyHS0");

                if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {

                    SerialPort sp = (SerialPort) id.open("SerialExample", 30);

                    sp.setSerialPortParams(9600, SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                    sp.enableReceiveTimeout(1000);
                    sp.enableReceiveThreshold(0);

                    InputStream is = sp.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader buf = new BufferedReader(isr);

                    System.out.println("Scanning port " + sp.getName());

                    // try each port few times before giving up
                    for (int i = 0; i < 5; i++) {
                        try {
                            String data = buf.readLine();
                            if (SentenceValidator.isValid(data)) {
                                System.out.println("NMEA data found!");
                                return sp;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    is.close();
                    isr.close();
                    buf.close();
                }
            //}
            System.out.println("NMEA data was not found..");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Init serial port and reader.
     */
    private void init() {
        try {
            SerialPort sp = getSerialPort();

            if (sp != null) {
                InputStream is = sp.getInputStream();
                SentenceReader sr = new SentenceReader(is);
                sr.addSentenceListener(this);
                sr.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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
