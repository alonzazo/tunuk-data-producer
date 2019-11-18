package producers.APCDataProducer;

import producers.DataProducer;
import utils.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HellaAPCECORS485DataProducer implements DataProducer, Runnable {

    private enum DoorState {DOOR_OPENED, DOOR_CLOSED, DOOR_UNKNOWN};

    private EventBus eventBus;

    private static class DataProducerIdentity {
        String brand = "Hella",
                model= "APC-ECO-RS485",
                serial="",
                dataScheme="APC",
                controllerVersion="1.0",
                description="";
    }
    private DataProducerIdentity identity = new DataProducerIdentity();

    public HellaAPCECORS485DataProducer(int doorId, String ipAddress, int port, EventBus eventbus) {
        this.doorProperties.id = doorId;
        this.doorProperties.ipAddress = ipAddress;
        this.doorProperties.port = port;
        this.doorProperties.date = new Date(System.currentTimeMillis());
        this.doorProperties.isOpened = false;
        this.eventBus = eventbus;

        identity.description = "door-" + doorId;
    }


    @Override
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    private Door doorProperties = new Door();

    private class Door {
        int id, port;
        String ipAddress;
        boolean isOpened;
        Date date;
    }

    private String sendRequest(String request) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);

        byte[] buffer = request.getBytes();

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(doorProperties.ipAddress), doorProperties.port);
        socket.send(packet);

        System.out.println("Mensaje enviado al Hella:\n" + request);

        DatagramPacket receivingData = new DatagramPacket(new byte[100], 100);

        socket.receive(receivingData);

        socket.close();

        String messageReceived = new String(receivingData.getData());

        System.out.println("Mensaje recibido del Hella:\n" + messageReceived);

        return messageReceived;
    }

    @Override
    public void startProduction() throws Exception {

        Thread thread = new Thread(this);
        thread.start();

    }

    @Override
    public void run() {

        while (true){

            try {
                //Si esta cerrada, es decir, = 1
                DoorState doorState = getDoorState();
                System.out.println("El estado de la puerta es: " + doorState);
                if (doorState == DoorState.DOOR_CLOSED){

                    System.out.println("La puerta está cerrada");
                    //  Actualizamos el estado de la puerta a cerrado
                    doorProperties.isOpened = false;
                    //  Consultamos la cantidad de entradas y salidas 'VDV2bE'
                    Map<String, String> extendedData = getExtendedData();
                    System.out.println("Los datos se han extraido " + extendedData.toString());

                    int inPassengers = Integer.parseInt(extendedData.get("inPassengers"));
                    int outPassengers = Integer.parseInt(extendedData.get("outPassengers"));

                    long duration =  (Math.abs(new Date(System.currentTimeMillis()).getTime() - doorProperties.date.getTime())/1000);

                    //  Verificamos si hay datos disponibles. Si passengersIn o passengerOut son mayor que 0 y la fecha es diferente de 0
                    if ((inPassengers > 0 || outPassengers > 0) && duration != 0){
                        //      Se resetea el contador 'VDV2bF' + id
                        resetCounter();
                        System.out.println("El contador se ha reseteado");
                        //      Si se resetea bien
                        //          la fecha se setea en 0
                        doorProperties.date = new Date(0);
                        //      Si no
                        //          Se notifica el error
                        //  Si no hay datos disponibles

                        extendedData.put("doorId",String.valueOf(doorProperties.id));
                        extendedData.put("duration", String.valueOf(duration));

                        //Se coloca la identidad
                        putIdentityToData(extendedData);

                        System.out.println(extendedData);

                        getEventBus().publishData(this.getClass(), extendedData);
                    } else {
                        throw new Exception("There is no data in the door " + doorProperties.id);
                    }
                    //      Notificamos el error
                    //Si la puerta está abierta, es decir, = 0
                } else if (doorState == DoorState.DOOR_OPENED){
                    //  Notificamos que está abierta
                    System.out.println("Door " + doorProperties.id + "is opened");
                    //  Actualizamos el estado de la puerta a abierto
                    if (!doorProperties.isOpened) doorProperties.date = new Date(System.currentTimeMillis());
                    doorProperties.isOpened = true;

                    //Si el estado de la puerta es desconocido
                } else {
                    //  Notificamos que es desconocido
                    throw new Exception("The state of door " + doorProperties.id + " is unknown: " + doorState);
                }
            } catch (Exception e){
                e.printStackTrace();
                System.out.println(e.getMessage());
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    private void putIdentityToData(Map<String,String> data){
        data.put("brand", identity.brand);
        data.put("model", identity.model);
        data.put("serial", identity.serial);
        data.put("data-scheme",identity.dataScheme);
        data.put("controller-version", identity.controllerVersion);
    }

    private String resetCounter() throws Exception{
        return sendRequest("VDV2bF" + doorProperties.id);
    }


    private DoorState getDoorState() throws Exception{
        char doorState = sendRequest("VDV2bW"+doorProperties.id).charAt(6);

        switch (doorState){
            case '0':
                return DoorState.DOOR_OPENED;
            case '1':
                return DoorState.DOOR_CLOSED;
            default:
                return DoorState.DOOR_UNKNOWN;
        }
    }

    public Map<String,String> getInOutPassengersCount() throws IOException {
        String inAndOutsQuantities = sendRequest("VDV2bE" + doorProperties.id);

        //  Se da formato a la respuesta.
        //  ____________________________________________________________________________________________________________
        //  |                                                                                                          |
        //  |   Patrón de respuesta:                                                                                   |
        //  |                                                                                                          |
        //  |       VDV2b\d{2}\d{2}                                                                                    |
        //  |__________________________________________________________________________________________________________|

        //  Se le quita 'VDV3b' a la respuesta
        inAndOutsQuantities = inAndOutsQuantities.replace("VDV2b","");
        //  Se formatea a Hex
        inAndOutsQuantities = formatASCIItoHex(inAndOutsQuantities);

        //  Se parsea a Int los caracteres del 0 al 2 en Hexadecimal
        int inPassengers = Integer.parseInt(inAndOutsQuantities.substring(0,2), 16);
        //  Se parsea a Int los caracteres del 0 al 2 en Hexadecimal
        int outPassengers = Integer.parseInt(inAndOutsQuantities.substring(2,4), 16);

        Map<String, String> inAndOutPassengers = new HashMap<>();
        inAndOutPassengers.put("inPassengers", String.valueOf(inPassengers));
        inAndOutPassengers.put("outPassengers", String.valueOf(outPassengers));

        return inAndOutPassengers;
    }

    public Map<String,String> getExtendedData() throws IOException {
        String response = sendRequest("VDV2bX" + doorProperties.id);

        //  Se da formato a la respuesta.
        //  ____________________________________________________________________________________________________________
        //  |                                                                                                          |
        //  |   Patrón de respuesta:                                                                                   |
        //  |                                                                                                          |
        //  |       VDV2bwHoHdHeHHHHxHHHHzHfHH                                                                         |
        //  |__________________________________________________________________________________________________________|

        //  Se le quita 'VDV3b' a la respuesta
        response = response.replace("VDV2b","");

        Map<String, String> qualifierValueMap = parseQualifierValueMap(response);

        Map<String, String> extendedData = new HashMap<>();

        qualifierValueMap.forEach((qualifier, value) -> {
            //Se manejan los qualifiers especiales
            if (qualifier.equals("e")){
                String inPassengersValue = value.substring(0,2);
                String outPassengersValue = value.substring(2,4);

                // Se formatean los numeros a HEX
                String inPassengersHexValue = formatASCIItoHex(inPassengersValue);
                String outPassengersHexValue = formatASCIItoHex(outPassengersValue);
                // Se convierten a DEC
                int inPassengersDecimalValue = Integer.parseInt(inPassengersHexValue,16);
                int outPassengersDecimalValue = Integer.parseInt(outPassengersHexValue,16);

                extendedData.put("inPassengers", String.valueOf(inPassengersDecimalValue));
                extendedData.put("outPassengers", String.valueOf(outPassengersDecimalValue));
            } else {
                // Se formatean los numeros a HEX
                String hexValue = formatASCIItoHex(value);
                // Se convierten a DEC
                int decimalValue = Integer.parseInt(hexValue,16);
                // Se le asigna una etiqueta
                String label = getLabelByQualifier(qualifier);

                extendedData.put(label,String.valueOf(decimalValue));
            }
        });

        return extendedData;
    }

    private Map<String,String> parseQualifierValueMap(String string){
        Map<String,String> qualifierValueMap = new HashMap<>();
        String currentQualifier = "";
        String currentValue = "";
        for (int i = 0; i < string.length(); i++){
            String currentCharacter = String.valueOf(string.charAt(i));

            if (currentCharacter.matches("[a-z]")){
                if (!currentValue.equals("")){
                    qualifierValueMap.put(currentQualifier,currentValue);
                }
                currentQualifier = currentCharacter;
                currentValue = "";
            }
            else
                if (currentCharacter.matches("[0-9:;<=>?]"))
                    currentValue += currentCharacter;
        }
        qualifierValueMap.put(currentQualifier,currentValue);
        return qualifierValueMap;
    }

    private String formatASCIItoHex(String ascii){
        //  ____________________________________________________________________________________________________________
        //  |                                                                                                          |
        //  |   Las H son hexadecimal con codificación:                                                          |
        //  |   'A' es ':'                                                                                             |
        //  |   'B' es ';'                                                                                             |
        //  |   'C' es '<'                                                                                             |
        //  |   'D' es '='                                                                                             |
        //  |   'E' es '>'                                                                                             |
        //  |   'F' es '?'                                                                                             |
        //  |__________________________________________________________________________________________________________|
        //  Se cambia los : por A
        ascii = ascii.replaceAll(":","A");
        //  Se cambia los ; por B
        ascii = ascii.replaceAll(";","B");
        //  Se cambia los < por C
        ascii = ascii.replaceAll("<","C");
        //  Se cambia los = por D
        ascii = ascii.replaceAll("=","D");
        //  Se cambia los > por E
        ascii = ascii.replaceAll(">","E");
        //  Se cambia los ? por F
        ascii = ascii.replaceAll("\\?","F");

        return ascii;
    }

    private String getLabelByQualifier(String qualifier){
        switch (qualifier){
            case "w":
                return "doorOpenStatus";
            case "o":
                return "diagnosticStatus";
            case "d":
                return "digitalInputStatus";
            case "e":
                return "currentPassengerCounts";
            case "x":
                return "currentPassengerExchangeTime";
            case "z":
                return "currentClearZone";
            case "f":
                return "currentFillLevel";
            default:
                return "qualifierNotIdentified";
        }
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
}
