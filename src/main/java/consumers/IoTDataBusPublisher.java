package consumers;

import connectors.IoTConnector;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class IoTDataBusPublisher implements Subscriber {

    private IoTConnector ioTConnector;
    private Function<List<Map<String,String>>, String> handlerFunction;
    private String topic;

    public IoTDataBusPublisher(IoTConnector ioTConnector, String topic, Function<List<Map<String,String>>, String> handlerFunction) {
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
    }

    public IoTDataBusPublisher(IoTConnector ioTConnector, String topic, Function<List<Map<String,String>>, String> handlerFunction, int sleepTime) {
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
    }

    @Override
    public void handleDataBus(List<Map<String, String>> data) {
        try {
            String message = handlerFunction.apply(data);
            ioTConnector.publish(topic,message);
        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
