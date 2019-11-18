package factories;

import utils.DataBus;
import utils.MicrobatchDataBus;

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
