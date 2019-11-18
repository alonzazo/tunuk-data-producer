/*
package utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SyncronizedDataBus implements DataBus {

    private Map<Class<?>, List<Map<String,String>>> dataBus;

    public SyncronizedDataBus() {
        dataBus = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized Map<Class<?>, List<Map<String, String>>> getDataBus() {
        return dataBus;
    }

    public synchronized List<Map<String, String>> getMergedData() {
        List<Map<String,String>> result = new LinkedList<>();
        for (List<Map<String,String>> data: dataBus.values())
            result.addAll(data);
        flush();
        return result;
    }

    @Override
    public synchronized void flush() {
        for (List list:
             dataBus.values()) {
            list.clear();
        }
        dataBus.clear();
    }

    @Override
    public synchronized void publishData(Class<?> dataProducer, Map<String, String> data) {
        List<Map<String, String>> dataBusFromDataDriver = dataBus.get(dataProducer);
        if (dataBusFromDataDriver != null){
            dataBusFromDataDriver.add(data);
        } else {
            dataBusFromDataDriver = new LinkedList<>();
            dataBusFromDataDriver.add(data);
            dataBus.put(dataProducer,dataBusFromDataDriver);
        }
    }
}
*/
