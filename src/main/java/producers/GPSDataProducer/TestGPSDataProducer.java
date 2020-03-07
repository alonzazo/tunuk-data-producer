package producers.GPSDataProducer;

import eventbuses.EventBus;
import producers.DataProducer;

import java.util.HashMap;
import java.util.Map;

public class TestGPSDataProducer implements DataProducer {

    private EventBus eventBus;
    private Integer generationIntervalMs;

    public TestGPSDataProducer(EventBus eventBus) {
        generationIntervalMs = 500;
        this.eventBus = eventBus;
    }

    private static class DataProducerIdentity {
        String brand = "TNKCorporation",
                model= "TestGPSDataProducer",
                serial="",
                dataScheme="GPS",
                controllerVersion="1.0";
    }
    private TestGPSDataProducer.DataProducerIdentity identity = new TestGPSDataProducer.DataProducerIdentity();

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void setBrand(String brandName) {
        identity.brand = brandName;
    }

    @Override
    public String getBrand() {
        return identity.brand;
    }

    @Override
    public void setModel(String modelName) {
        identity.model = modelName;
    }

    @Override
    public String getModel() {
        return identity.model;
    }

    @Override
    public void setSerial(String serial) {
        identity.serial = serial;
    }

    @Override
    public String getSerial() {
        return identity.serial;
    }

    @Override
    public void setDataScheme(String dataScheme) {
        identity.dataScheme = dataScheme;
    }

    @Override
    public String getDataScheme() {
        return identity.dataScheme;
    }

    @Override
    public void setControllerVersion(String controllerVersion) {
        identity.controllerVersion = controllerVersion;
    }

    @Override
    public String getControllerVersion() {
        return identity.controllerVersion;
    }

    @Override
    public void startProduction() throws Exception {

        Thread thread = new Thread(()-> {
            while (true) {

                try {
                    Map<String, String> data = new HashMap<>();
                    data.put("longitude", String.valueOf(-84.0608332  + Math.random()));
                    data.put("latitude", String.valueOf(9.8963963 + Math.random()));
                    data.put("altitude", String.valueOf(Math.random() * 100));

                    //Agregamos la identidad
                    putIdentityToData(data);

                    getEventBus().publishData(this.getClass(), data);

                    Thread.sleep(generationIntervalMs);
                }catch (Exception ignore){}

            }
        });
        thread.start();

    }

    private void putIdentityToData(Map<String,String> data){
        data.put("brand", identity.brand);
        data.put("model", identity.model);
        data.put("serial", identity.serial);
        data.put("data-scheme",identity.dataScheme);
        data.put("controller-version", identity.controllerVersion);
    }
}
