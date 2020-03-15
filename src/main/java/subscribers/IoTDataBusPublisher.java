package subscribers;

import connectors.IoTConnector;
import connectors.IoTConnectorException;
import faulttolerance.PersistentQueue;
import faulttolerance.PersistentQueueException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class IoTDataBusPublisher implements Subscriber {

    private IoTConnector ioTConnector;
    private Function<List<Map<String, String>>, String> handlerFunction;
    private String topic;
    private PersistentQueue persistentQueue;
    private AtomicBoolean connected;
    private ExecutorService executor = Executors.newFixedThreadPool(100);

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

            String messageOld = "";

            long size = -1;

            try {

                synchronized (getPersistentQueue()){
                    messageOld = getPersistentQueue().pollMessage();
                    size = getPersistentQueue().size();
                }

                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MENSAJE]: Se han encontrado " + size + " mensajes en cola de respaldo -> Intentando retransmitir...");

                while (messageOld != null) {

                    sendMessageAsAThread(messageOld);

                    synchronized (getPersistentQueue()){
                        messageOld = getPersistentQueue().pollMessage();
                    }

                    if (!connected.get()) break;

                }
            } catch (PersistentQueueException e) {
                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [EXCEPCIÓN]: No se logró extraer o guardar mensaje de cola de respaldo ");
            }
        }


    }

    private void sendMessageAsAThread(String message) {
        executor.execute(() -> {

            try {

                ioTConnector.publish(topic, message);

                connected.set(true);

                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MENSAJE]: Mensaje enviado con éxito: " + message);

            } catch (IoTConnectorException e) {

                connected.set(false);

                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MENSAJE]: Fallo en la conexión -> Intentando guardar en cola de respaldo...");
                try {
                    if (message.contains("APC")) {
                        long size = -1;

                        synchronized (getPersistentQueue()){
                            getPersistentQueue().pushMessage(message);
                            size = getPersistentQueue().size();
                        }

                        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MENSAJE]: Mensaje sensible " + size + " guardado con éxito: " + message);

                    } else {
                        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MENSAJE]: Mensaje dispensable, no se guardó: " + message);
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
    }

    private PersistentQueue getPersistentQueue() {
        return this.persistentQueue;
    }

}
