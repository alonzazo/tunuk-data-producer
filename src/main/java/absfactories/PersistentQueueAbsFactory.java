package absfactories;

import factories.persistentqueuesfactories.BerkleyDBPersistentQueueFactory;
import factories.persistentqueuesfactories.PersistentQueueFactory;

public class PersistentQueueAbsFactory {

    private PersistentQueueAbsFactory(){}

    public static PersistentQueueFactory createFactory(PersistentQueueType type) throws PersistentQueueNotFoundException {
        switch (type){
            case BERKLEY_DB:
                return new BerkleyDBPersistentQueueFactory();
            default:
                throw new PersistentQueueNotFoundException();
        }
    }
}
