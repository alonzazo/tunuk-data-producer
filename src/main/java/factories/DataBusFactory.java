package factories;

import utils.DataBus;
import utils.SyncronizedDataBus;

public class DataBusFactory {

    public static DataBus create(DataBusType type){
        switch (type){
            case SYNCRONIZED:
                return createDataBusSyncronized();
            default:
                return null;
        }
    }

    private static DataBus createDataBusSyncronized() {
        return new SyncronizedDataBus();
    }
}
