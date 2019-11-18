package factories;

import utils.MicrobatchDataBus;

public class MicrobatchDataBusFactory implements EventBusFactory{


    public MicrobatchDataBus create(long interval){
        return new MicrobatchDataBus(interval);
    }

    public MicrobatchDataBus create(){
        return new MicrobatchDataBus();
    }

}
