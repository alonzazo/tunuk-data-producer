package consumers;

import connectors.IoTConnector;
import utils.DataBus;

import java.util.function.Function;

import static java.lang.Thread.sleep;

public class IoTDataBusPublisher implements DataBusPublisher {

    private DataBus dataBus;
    private IoTConnector ioTConnector;
    private Function<DataBus, String> handlerFunction;
    private String topic;
    private int sleepTime;

    public IoTDataBusPublisher(DataBus dataBus, IoTConnector ioTConnector, String topic, Function<DataBus, String> handlerFunction) {
        this.dataBus = dataBus;
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
        sleepTime = 1000;
    }

    public IoTDataBusPublisher(DataBus dataBus, IoTConnector ioTConnector, String topic, Function<DataBus, String> handlerFunction, int sleepTime) {
        this.dataBus = dataBus;
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
        this.sleepTime = sleepTime;
    }

    @Override
    public void setDataBus(DataBus dataBus) {
        this.dataBus = dataBus;
    }

    @Override
    public DataBus getDataBus() {
        return dataBus;
    }

    @Override
    public void startPublish() {
        Thread threadPublisher = new Thread(() -> {
            try {
                while (true){
                    String message = handlerFunction.apply(getDataBus());
                    ioTConnector.publish(topic,message);
                    sleep(sleepTime);
                }
            } catch (Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        });
        threadPublisher.start();
    }

}
