package subscribers;

import connectors.IoTConnector;
import connectors.IoTConnectorException;
import faulttolerance.PersistentQueue;
import faulttolerance.PersistentQueueException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class IoTDataBusPublisher implements Subscriber {

    private IoTConnector ioTConnector;
    private Function<List<Map<String, String>>, String> handlerFunction;
    private String topic;
    private PersistentQueue persistentQueue;
    private AtomicBoolean connected;

    public IoTDataBusPublisher(IoTConnector ioTConnector, String topic, Function<List<Map<String, String>>, String> handlerFunction) {
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
        this.connected = new AtomicBoolean(false);
    }

    public IoTDataBusPublisher(IoTConnector ioTConnector, String topic, Function<List<Map<String, String>>, String> handlerFunction, int sleepTime) {
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
        this.connected = new AtomicBoolean(false);
    }

    public IoTDataBusPublisher(IoTConnector ioTConnector, String topic, Function<List<Map<String, String>>, String> handlerFunction, PersistentQueue persistentQueue) {
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
        this.persistentQueue = persistentQueue;
        this.connected = new AtomicBoolean(false);
    }


    @Override
    public void handleDataBus(List<Map<String, String>> data) {
        String message = handlerFunction.apply(data);

        sendMessageAsAThread(message);

        if (connected.get()) {

            long size = persistentQueue.size();

            try {
                while (size > 0) {

                    System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MENSAJE]: Se han encontrado " + size + " mensajes en cola de respaldo -> Intentando retransmitir...");

                    String messageOld = "";

                    messageOld = persistentQueue.pollMessage();

                    sendMessageAsAThread(messageOld);

                    size = persistentQueue.size();

                    if (!connected.get()) break;

                }
            } catch (PersistentQueueException e) {
                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [EXCEPCIÓN] No se logró extraer o guardar mensaje de cola de respaldo ");
            }
        }


    }

    private void sendMessageAsAThread(String message) {
        Thread thread = new Thread(() -> {

            try {

                ioTConnector.publish(topic, message);

                connected.compareAndSet(false, true);

                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MESSAGE_SENT]: " + message);

            } catch (IoTConnectorException e) {

                connected.compareAndSet(true, false);

                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MENSAJE]: Fallo en la conexión -> Intentando guardar en cola de respaldo...");
                try {
                    if (message.contains("APC")) {

                        persistentQueue.pushMessage(message);

                        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MENSAJE] Tamaño de cola de respaldo: " + persistentQueue.size() + ": Mensaje con dato sensible: Guardado con éxito: " + message);

                    } else {
                        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MENSAJE] Mensaje dispensable, no se guardó: " + message);
                    }

                } catch (Exception exception) {

                    System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [EXCEPCION]: " + exception.getMessage());
                    exception.printStackTrace();

                }

            } catch (Throwable e) {
                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [EXCEPCION]: " + e.getMessage());
                e.printStackTrace();
            }
        });

        thread.start();
    }

}
