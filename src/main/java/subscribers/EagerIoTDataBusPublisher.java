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
import java.util.function.Function;

public class EagerIoTDataBusPublisher implements Subscriber {

    private IoTConnector ioTConnector;
    private Function<List<Map<String, String>>, String> handlerFunction;
    private String topic;
    private PersistentQueue persistentQueue;
    private ExecutorService executor = Executors.newFixedThreadPool(100);

    public EagerIoTDataBusPublisher(IoTConnector ioTConnector, String topic, Function<List<Map<String, String>>, String> handlerFunction) {
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
    }

    public EagerIoTDataBusPublisher(IoTConnector ioTConnector, String topic, Function<List<Map<String, String>>, String> handlerFunction, int sleepTime) {
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
    }

    public EagerIoTDataBusPublisher(IoTConnector ioTConnector, String topic, Function<List<Map<String, String>>, String> handlerFunction, PersistentQueue persistentQueue) {
        this.ioTConnector = ioTConnector;
        this.handlerFunction = handlerFunction;
        this.topic = topic;
        this.persistentQueue = persistentQueue;

        Thread senderService = new Thread(() -> {

            String inflightMessage = null;

            while (true) {

                synchronized (getPersistentQueue()) {

                    try {
                        inflightMessage = getPersistentQueue().peekMessage();
                    } catch (PersistentQueueException ex) {
                        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [EXCEPCION]: No se pudo sacar datos de la cola: " + ex.getMessage());
                    }

                }

                if (inflightMessage != null) {
                    try {

                        long size = -1;

                        long startTime = System.currentTimeMillis();

                        ioTConnector.publish(this.topic, inflightMessage);

                        synchronized (getPersistentQueue()) {
                            getPersistentQueue().pollMessage();
                            size = getPersistentQueue().size();
                        }

                        long finishTime = System.currentTimeMillis();

                        long duration = finishTime - startTime;

                        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MENSAJE]: Mensaje enviado, recibido y quitado de cola exitosamente en " + duration + "ms. Tamaño de cola: " + size + ": " + inflightMessage);

                    } catch (PersistentQueueException ex) {
                        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [EXCEPCION]: No se pudo sacar datos de la cola: " + ex.getMessage());
                    } catch (IoTConnectorException ex) {
                        System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [EXCEPCION]: Fallo en la conexión. Mensaje sensible no enviado, se mantiene en cola de respaldo.");
                    }
                }

            }
        });

        senderService.setName("eager-publisher-thread");
        senderService.start();
    }


    @Override
    public void handleDataBus(List<Map<String, String>> data) {

        // Se compone el mensaje
        String message = handlerFunction.apply(data);

        if (message.contains("APC")) {
            try {

                long size = -1;
                // Se coloca en la cola
                synchronized (getPersistentQueue()) {
                    getPersistentQueue().pushMessage(message);
                    size = getPersistentQueue().size();
                }

                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [MENSAJE]: Mensaje sensible guardado con éxito: " + size + ": " + message);
            } catch (PersistentQueueException ex) {
                System.out.println(Instant.now() + " " + Thread.currentThread().getName() + " [EXCEPCION]: No se pudo guardar el mensaje en la cola de respaldo: " + ex.getMessage() + message);
            }
        } else {
            sendMessageAsAThread(message);
        }

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

    private PersistentQueue getPersistentQueue() {
        return this.persistentQueue;
    }

}
