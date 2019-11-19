package subscribers;

import eventbuses.DataBus;

public interface DataBusConsumer {
    void setDataBus(DataBus dataBus);
    DataBus getDataBus();
}
