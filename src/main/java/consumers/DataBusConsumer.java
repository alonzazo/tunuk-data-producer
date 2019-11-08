package consumers;

import utils.DataBus;

public interface DataBusConsumer {
    void setDataBus(DataBus dataBus);
    DataBus getDataBus();
}
