package fr.layer4.hhsl.commands;

import fr.layer4.hhsl.local.LocalStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@Slf4j
@ShellComponent
public class ConfigureCommands {

    @Autowired
    private LocalStore localStore;

    @ShellMethod(key = "init", value = "Init local database", group = "Configuration")
    public void init(boolean secured) {
        localStore.init(secured);
    }

    public Availability initAvailability() {
        return !localStore.isReady()
                ? Availability.available()
                : Availability.unavailable("Local database is already ready, use clean before re-init");
    }

    @ShellMethod(key = "clean", value = "Clean local database", group = "Configuration")
    public void clean() {
        localStore.purge();
    }

    public Availability cleanAvailability() {
        return localStore.isReady()
                ? Availability.available()
                : Availability.unavailable("Local database is not ready");
    }

    @ShellMethod(key = "change-password", value = "Init local database", group = "Configuration")
    public void change() {
        localStore.changePassword();
    }
}
