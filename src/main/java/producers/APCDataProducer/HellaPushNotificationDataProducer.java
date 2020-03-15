package producers.APCDataProducer;

import com.hella.bike.api.*;
import com.hella.bike.api.implementation.DeviceManagerImpl;
import eventbuses.EventBus;
import producers.DataProducer;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class HellaPushNotificationDataProducer implements DataProducer, Runnable {

    private EventBus eventBus;
    private int port;
    private String[] managedDevices;

    private static class DataProducerIdentity {
        String brand = "Hella",
                model = "PushNotification-v1.10",
                serial = "",
                dataScheme = "APC",
                controllerVersion = "1.0",
                description = "";
    }

    private HellaPushNotificationDataProducer.DataProducerIdentity identity = new HellaPushNotificationDataProducer.DataProducerIdentity();

    public HellaPushNotificationDataProducer(EventBus eventBus, int port, String[] managedDevices) {
        this.eventBus = eventBus;
        this.port = port;
        this.managedDevices = managedDevices;
    }

    public HellaPushNotificationDataProducer(EventBus eventBus, int port) {
        this.eventBus = eventBus;
        this.port = port;
        this.managedDevices = new String[0];
    }

    public HellaPushNotificationDataProducer(EventBus eventBus) {
        this.eventBus = eventBus;
        this.port = 80;
        this.managedDevices = new String[0];
    }

    @Override
    public void setBrand(String brandName) {
        identity.brand = brandName;
    }

    @Override
    public String getBrand() {
        return identity.brand;
    }

    @Override
    public void setModel(String modelName) {
        identity.model = modelName;
    }

    @Override
    public String getModel() {
        return identity.model;
    }

    @Override
    public void setSerial(String serial) {
        identity.serial = serial;
    }

    @Override
    public String getSerial() {
        return identity.serial;
    }

    @Override
    public void setDataScheme(String dataScheme) {
        identity.dataScheme = dataScheme;
    }

    @Override
    public String getDataScheme() {
        return identity.dataScheme;
    }

    @Override
    public void setControllerVersion(String controllerVersion) {
        identity.controllerVersion = controllerVersion;
    }

    @Override
    public String getControllerVersion() {
        return identity.controllerVersion;
    }

    @Override
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public void startProduction() throws Exception {

        Thread thread = new Thread(this);
        thread.setName("PushNotificationThread");
        thread.start();

    }

    @Override
    public void run() {

        DeviceManager deviceManager = DeviceManagerImpl.getDeviceManager();

        if (managedDevices.length > 0)
            for (String managedDevice : managedDevices) {
                deviceManager.acceptDevice(managedDevice);
            }

        Application countingApp = deviceManager.getApplication("counting-sub");
        if (null == countingApp) {
            countingApp = deviceManager.createApplication("counting-sub");

            SubscriptionManager subscriptionManager = countingApp.getSubscriptionManager();

            try {
                subscriptionManager.subscribeAlive(new TimeTriggerImpl(DatatypeFactory.newInstance().newDuration(300000), DatatypeFactory.newInstance().newDuration(60000)));
            } catch (DatatypeConfigurationException e) {
                e.printStackTrace();
            }

            subscriptionManager.subscribeSafeCounting("counting-sub", new EventTriggerImpl(CountEventType.ALWAYS, 0));

        }

        countingApp.addCountingListener(countingEvent -> {

            Map<String, String> data = new HashMap<>();

            data.put("inPassengers", String.valueOf(countingEvent.getCountIn()));

            data.put("outPassengers", String.valueOf(countingEvent.getCountOut()));

            data.put("doorId", countingEvent.getCustomerId().charAt(countingEvent.getCustomerId().length() - 1) == 'D' ? String.valueOf(1) : String.valueOf(2));

            data.put("timestamp", Instant.ofEpochMilli(countingEvent.getTimeStamp().getTime()).toString());

            putIdentityToData(data);

            getEventBus().publishData(this.getClass(), data);

            deviceManager.saveConfiguration();

        });

        deviceManager.start(this.port);

    }

    private void putIdentityToData(Map<String, String> data) {
        data.put("brand", identity.brand);
        data.put("model", identity.model);
        data.put("serial", identity.serial);
        data.put("data-scheme", identity.dataScheme);
        data.put("controller-version", identity.controllerVersion);
    }
}
