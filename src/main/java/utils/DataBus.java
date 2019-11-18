package utils;

import java.util.List;
import java.util.Map;

public interface DataBus extends EventBus {
    /**
     * Return time interval miliseconds of each publication of DataBus
     * @return
     */
    long getInterval();

    /**
     * Get the data bus with all data for each dataProducer
     * @return databus
     */
    Map<Class<?>, List< Map<String,String> > >  getDataBus();

    /**
     * Flush the data bus, it leaves it empty.
     */
    void                                        flush();

    /**
     * Starts the publication of data to subscribers
     */
    void                                        startPublication();

}
