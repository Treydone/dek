package fr.layer4.hhsl.commands;

import fr.layer4.hhsl.local.LocalStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

@ShellComponent
public class UnlockCommands {

    @Autowired
    private LocalStore localStore;

    @ShellMethodAvailability(value = "*")
    public Availability availabilityAfterUnlock() {
        return localStore.isReady() && !localStore.isUnlocked()
                ? Availability.available()
                : Availability.unavailable("Secret store is not unlock");
    }

    @ShellMethod(value = "Unlock", group = "Main")
    public String unlock() {
        localStore.unlock();
        return "OK";
    }
}
