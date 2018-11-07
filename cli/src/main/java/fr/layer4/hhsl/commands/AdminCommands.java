package fr.layer4.hhsl.commands;

/*-
 * #%L
 * HHSL
 * %%
 * Copyright (C) 2018 Layer4
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import fr.layer4.hhsl.Constants;
import fr.layer4.hhsl.store.LockableStore;
import fr.layer4.hhsl.store.Store;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    @ShellMethod(key = "clean", value = "Clean all", group = "Configuration")
    public void clean() throws IOException {
        cleanStore();
        cleanArchives();
    }

    @ShellMethod(key = "clean store", value = "Clean local database", group = "Configuration")
    public void cleanStore() {
        store.purge();
    }

    @ShellMethod(key = "clean archive", value = "Clean archives", group = "Configuration")
    public void cleanArchives() throws IOException {
        String basePath = Constants.getRootPath();
        String archivesPath = basePath + File.separator + Constants.ARCHIVES;
        Files.deleteIfExists(Paths.get(archivesPath));
    }

    public Availability cleanStoreAvailability() {
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
