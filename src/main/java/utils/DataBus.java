package utils;

import java.util.List;
import java.util.Map;

public interface DataBus {

    Map<Class<?>, List< Map<String,String> > >  getDataBus();
    void                                        flush();
    Map<Class<?>, List<Map<String, String> > >  consumeData();
    List<Map<String, String>>                   consumeMergedData();
    void                                        publishData(Class<?> dataDriver, Map<String,String> data);

}
