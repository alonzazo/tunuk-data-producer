package subscribers;

import connectors.IoTConnector;
import connectors.IoTConnectorException;
import faulttolerance.PersistentQueue;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class IoTDataBusPublisher implements Subscriber {

    private IoTConnector ioTConnector;
    private Function<List<Map<String,String>>, String> handlerFunction;
    private String topic;
    private PersistentQueue persistentQueue;

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

    public IoTDataBusPublisher(IoTConnector ioTConnector, String topic, Function<List<Map<String,String>>, String> handlerFunction, PersistentQueue persistentQueue) {
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
        this.persistentQueue = persistentQueue;
    }



    @Override
    public void handleDataBus(List<Map<String, String>> data) {
        String message = "";
        try {

            message = handlerFunction.apply(data);
            ioTConnector.publish(topic,message);
            System.out.println(message);

            //TODO Evaluar si es mejor enviar respaldos junto con mensajes
            long size = persistentQueue.size();
            if (size > 0){
                System.out.println("[MENSAJE] Se han encontrado " + size + " mensajes en cola de respaldo -> Intentando retransmitir...");
                message = persistentQueue.pollMessage();
                ioTConnector.publish(topic, message);
            }

        } catch (IoTConnectorException e){

            System.out.println("[MENSAJE] Fallo en la conexión -> Intentando guardar en cola de respaldo...");
            try {

                persistentQueue.pushMessage(message);
                System.out.println("[MENSAJE] Guardado con éxito");

            } catch (Exception exception){

                exception.printStackTrace();

            }

        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
