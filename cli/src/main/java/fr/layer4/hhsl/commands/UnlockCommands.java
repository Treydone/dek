package fr.layer4.hhsl.commands;

import fr.layer4.hhsl.store.LockableStore;
import fr.layer4.hhsl.store.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

@ShellComponent
public class UnlockCommands {

    @Autowired
    private Store store;

    @ShellMethodAvailability(value = "*")
    public Availability availability() {
        if (store instanceof LockableStore) {
            LockableStore lockableStore = (LockableStore) store;
            if (!lockableStore.isUnlocked()) {
                return Availability.available();
            }
        }
        return Availability.unavailable("Store is not lock");
    }

    @ShellMethod(value = "Unlock", group = "Main")
    public void unlock() {
        if (store instanceof LockableStore) {
            LockableStore lockableStore = (LockableStore) store;
            lockableStore.unlock();
        }
    }
}
