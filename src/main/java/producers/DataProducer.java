package producers;

import utils.EventBus;

public interface DataProducer {

    void setBrand(String brandName);
    String getBrand();
    void setModel(String modelName);
    String getModel();
    void setSerial(String serial);
    String getSerial();
    void setDataScheme(String dataScheme);
    String getDataScheme();
    void setControllerVersion(String controllerVersion);
    String getControllerVersion();

    EventBus getEventBus();
    void setEventBus(EventBus eventBus);
    void startProduction() throws Exception;

}
