package producers;

import utils.DataBus;

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

    DataBus getDataBus();
    void setDataBus(DataBus dataBus);
    void startProduction() throws Exception;

}
