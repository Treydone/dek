package fr.layer4.hhsl.commands;

import fr.layer4.hhsl.store.LockableStore;
import fr.layer4.hhsl.store.Store;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@Slf4j
@ShellComponent
public class AdminCommands {

    @Autowired
    private Store store;

    @ShellMethod(key = "init", value = "Init local database", group = "Configuration")
    public void init(boolean secured) {
        if (store instanceof LockableStore) {
            LockableStore lockableStore = (LockableStore) store;
            lockableStore.init(secured);
        } else {
            store.init();
        }
    }

    public Availability initAvailability() {
        return !store.isReady()
                ? Availability.available()
                : Availability.unavailable("Local database is already ready, use clean before re-init");
    }

    @ShellMethod(key = "clean", value = "Clean local database", group = "Configuration")
    public void clean() {
        store.purge();
    }

    public Availability cleanAvailability() {
        return store.isReady()
                ? Availability.available()
                : Availability.unavailable("Local database is not ready");
    }

    @ShellMethod(key = "change-password", value = "Init local database", group = "Configuration")
    public void change() {
        if (store instanceof LockableStore) {
            LockableStore lockableStore = (LockableStore) store;
            lockableStore.changePassword();
        }
    }
}
