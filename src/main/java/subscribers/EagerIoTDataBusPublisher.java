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

public class EagerIoTDataBusPublisher implements Subscriber {

    private IoTConnector ioTConnector;
    private Function<List<Map<String, String>>, String> handlerFunction;
    private String topic;
    private PersistentQueue persistentQueue;
    private ExecutorService executor = Executors.newFixedThreadPool(100);
    private AtomicBoolean connected;

    public EagerIoTDataBusPublisher(IoTConnector ioTConnector, String topic, Function<List<Map<String, String>>, String> handlerFunction) {
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
        this.connected = new AtomicBoolean(false);
    }

    public EagerIoTDataBusPublisher(IoTConnector ioTConnector, String topic, Function<List<Map<String, String>>, String> handlerFunction, int sleepTime) {
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
        this.connected = new AtomicBoolean(false);


    }

    public EagerIoTDataBusPublisher(IoTConnector ioTConnector, String topic, Function<List<Map<String, String>>, String> handlerFunction, PersistentQueue persistentQueue) {
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
        this.persistentQueue = persistentQueue;

        Thread senderService = new Thread(() -> {

            long size = getPersistentQueue().size();

            while (true) {

                if (size > 0) {

                    try {
                        String inflightMessage = getPersistentQueue().peekMessage();

                        if (inflightMessage == null)
                            continue;

                        long startTime = System.currentTimeMillis();

                        ioTConnector.publish(this.topic, inflightMessage);

                        getPersistentQueue().pollMessage();

                        long finishTime = System.currentTimeMillis();

                        long duration = finishTime - startTime;

                        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MENSAJE]: Mensaje enviado, recibido y quitado de cola exitosamente en " + duration + "ms. Tamaño de cola: " + getPersistentQueue().size() + ": " + inflightMessage);

                    } catch (PersistentQueueException ex) {
                        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [EXCEPCION]: No se pudo sacar datos de la cola: " + ex.getMessage());
                    } catch (IoTConnectorException ex) {
                        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [EXCEPCION]: Mensaje no enviado, revise su conexión");
                    }

                }

                size = getPersistentQueue().size();

            }
        });

        senderService.start();
    }


    @Override
    public void handleDataBus(List<Map<String, String>> data) {

        // Se compone el mensaje
        String message = handlerFunction.apply(data);

        if (message.contains("APC")) {
            try {
                // Se coloca en la cola
                getPersistentQueue().pushMessage(message);
                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MENSAJE]: Mensaje sensible guardado con éxito: " + getPersistentQueue().size() + ": " + message);
            } catch (PersistentQueueException ex) {
                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [EXCEPCION]: No se pudo guardar el mensaje en la cola de respaldo: " + ex.getMessage() + message);
            }
        } else {
            sendMessageAsAThread(message);
        }


        /*
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
        }*/


    }

    private void sendMessageAsAThread(String message) {
        executor.execute(() -> {
            try {

                ioTConnector.publish(topic, message);

                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MESSAGE_SENT]: " + message);

            } catch (IoTConnectorException e) {

                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [EXCEPCION]: Fallo en la conexión. Mensaje dispensable fue descartado.");

            } catch (Throwable e) {
                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [EXCEPCION]: " + e.getMessage());
                e.printStackTrace();
            }

        });
    }

    private synchronized PersistentQueue getPersistentQueue() {
        return this.persistentQueue;
    }

}
