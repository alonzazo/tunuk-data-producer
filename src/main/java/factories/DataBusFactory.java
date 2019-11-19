package factories;

import eventbuses.DataBus;
import eventbuses.MicrobatchDataBus;

public class DataBusFactory {

    public static DataBus create(EventBusType type, long interval){
        switch (type){
            case MICROBATCH:
                return new MicrobatchDataBus(interval);
            default:
                return null;
        }
    }
}
