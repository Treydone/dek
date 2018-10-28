package fr.layer4.hhsl.commands;

import fr.layer4.hhsl.store.LockableStore;
import fr.layer4.hhsl.store.Store;
import org.springframework.shell.Availability;

public class Avaibilities {

    public static Availability unlockedAndReady(Store store) {
        if (store instanceof LockableStore) {
            LockableStore lockableStore = (LockableStore) store;
            if (!lockableStore.isUnlocked()) {
                return Availability.unavailable("Store is not unlock");
            }
        }
        return store.isReady() ? Availability.available()
                : Availability.unavailable("Store is not ready");
    }
}
