package subscribers;

import java.util.List;
import java.util.Map;

public interface Subscriber {

    void handleDataBus(List<Map<String,String>> data);

}
