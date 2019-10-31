package drivers.APCDataDriver;

import drivers.DataDriver;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HellaAPCECORS485DataDriver implements DataDriver {
    public HellaAPCECORS485DataDriver(int doorId, String ipAddress, int port) {
        this.doorProperties.id = doorId;
        this.doorProperties.ipAddress = ipAddress;
        this.doorProperties.port = port;
        this.doorProperties.date = new Date(System.currentTimeMillis());
        this.doorProperties.isOpened = false;
    }

    private Door doorProperties = new Door();

    private class Door {
        int id, port;
        String ipAddress;
        boolean isOpened;
        Date date;
    }

    private String sendRequest(String request) throws IOException {
        DatagramSocket socket = new DatagramSocket(doorProperties.port, InetAddress.getByName(doorProperties.ipAddress));

        DatagramPacket sendingData = new DatagramPacket(request.getBytes(), request.length(), InetAddress.getByName(doorProperties.ipAddress), doorProperties.port);

        socket.send(sendingData);

        DatagramPacket receivingData = new DatagramPacket(new byte[100], 100);

        socket.close();

        return receivingData.toString();
    }

    @Override
    public Map<String, String> getData() throws Exception {
        //Consultamos el estado de la puerta 'VDV2bW'
        String doorState = sendRequest("VDV2bW"+doorProperties.id);

        doorState = doorState.replace("VDV2bW","");

        //Si esta cerrada, es decir, = 1
        if (doorState.equals("1")){
            //  Actualizamos el estado de la puerta a cerrado
            doorProperties.isOpened = false;
            //  Consultamos la cantidad de entradas y salidas 'VDV2bE'
            Map<String, String> extendedData = getExtendedData();

            int inPassengers = Integer.parseInt(extendedData.get("inPassengers"));
            int outPassengers = Integer.parseInt(extendedData.get("outPassengers"));



            long duration =  (Math.abs(new Date(System.currentTimeMillis()).getTime() - doorProperties.date.getTime())/1000);

            //  Verificamos si hay datos disponibles. Si passengersIn o passengerOut son mayor que 0 y la fecha es diferente de 0
            if ((inPassengers > 0 || outPassengers > 0) && duration != 0){
                //      Se resetea el contador 'VDV2bF' + id
                sendRequest("VDV2bF" + doorProperties.id);
                //      Si se resetea bien
                //          la fecha se setea en 0
                doorProperties.date = new Date(0);
                //      Si no
                //          Se notifica el error
                //  Si no hay datos disponibles

                extendedData.put("doorId",String.valueOf(doorProperties.id));
                extendedData.put("duration", String.valueOf(duration));
                return extendedData;
            } else {
                throw new Exception("There is no data in the door " + doorProperties.id);
            }
            //      Notificamos el error
            //Si la puerta está abierta, es decir, = 0
        } else if (doorState.equals("0")){
            //  Notificamos que está abierta
            System.out.println("Door " + doorProperties.id + "is opened");
            //  Actualizamos el estado de la puerta a abierto
            if (!doorProperties.isOpened) doorProperties.date = new Date(System.currentTimeMillis());
            doorProperties.isOpened = true;

            //Si el estado de la puerta es desconocido
        } else {
            //  Notificamos que es desconocido
            throw new Exception("The state of door " + doorProperties.id + " is unknown");
        }
        return null;
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
}
