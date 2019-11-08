package producers;

import utils.DataBus;

public interface DataProducer {

    DataBus getDataBus();
    void setDataBus(DataBus dataBus);
    void startProduction() throws Exception;

}
