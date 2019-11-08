package utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SyncronizedDataBus implements DataBus {

    private Map<Class<?>, List<Map<String,String>>> dataBus;

    public SyncronizedDataBus() {
        dataBus = new HashMap<>();
    }

    @Override
    public synchronized Map<Class<?>, List<Map<String, String>>> getDataBus() {
        return dataBus;
    }

    @Override
    public synchronized List<Map<String, String>> consumeMergedData() {
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
    public synchronized Map<Class<?>, List<Map<String, String>>> consumeData() {
        Map<Class<?> , List<Map<String,String>>> result = getDataBus();
        flush();
        return result;
    }

    @Override
    public synchronized void publishData(Class<?> dataDriver, Map<String, String> data) {
        List<Map<String, String>> dataBusFromDataDriver = dataBus.get(dataDriver);
        if (dataBusFromDataDriver != null){
            dataBusFromDataDriver.add(data);
        } else {
            dataBusFromDataDriver = new LinkedList<>();
            dataBusFromDataDriver.add(data);
            dataBus.put(dataDriver,dataBusFromDataDriver);
        }
    }
}
