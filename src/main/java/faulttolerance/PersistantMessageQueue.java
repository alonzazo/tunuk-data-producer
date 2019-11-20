package faulttolerance;


import java.io.File;
import java.util.List;
import java.util.Queue;

public class PersistantMessageQueue {

    private Queue<String> messagesQueue;
    private List<TransactionLog> logBuffer;
    private long currentTransactionNumber;

    private File file;
    private File WALFile;

    public String popMessage(){

        return messagesQueue.poll();
    }

    public void flush(){

    }

    public void fetch(){}

    public void pushMessage(String message){
        // Se registra begin en LogBuffer

        // Se escribe en el WALFile

        // Se agrega a la cola
        messagesQueue.add(message);

        // Se escribe en la cola del archivo + byte de enviado en 0, se extrae su posicion de inicio y fin

        // Se registra commit en LogBuffer con su posicion

        // Se escribe en el WALFile


    }

    public void restoreFrom(String filePath){

        //  Se inicializa el LogBuffer y la cola

        //  Cargar el archivo de la Bitacora

        //  Cargar el archivo de la Cola de Mensajes

        //  Se setea el estado de la transaccion actual = null

        //  Mientras hayan lineas restantes en la bitacora
        //      Lee la linea
        //      Si es una linea valida se parsea como TransactionLog y se agrega al LogBuffer

        //      Si el estado actual es (null o commited o abort)
        //          Si el estado de la transaccion leida es begin
        //              Se cambia el estado actual a begin
        //          Si no
        //              Se imprime el error
        //              Se salta la linea
        //      Si el estado actual es (begin)
        //          Si el estado de la transaccion leida es commit
        //              Se cambia el estado
        //              Se carga el dato del archivo de cola de mensajes al messagesQueue
        //          Si el estado de la transaccion leida es abort
        //          Si no
        //              Se registra un abort en la bitacora
        //              Se cambia el estado


    }


}
