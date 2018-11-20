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
import fr.layer4.hhsl.prompt.Prompter;
import fr.layer4.hhsl.store.SecuredStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@ShellComponent
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminCommands {

    private final SecuredStore store;
    private final Prompter prompter;

    @ShellMethod(key = "clean", value = "Clean all", group = "Configuration")
    public void clean() throws IOException {
        cleanStore();
        cleanArchives();
    }

    @ShellMethod(key = "clean store", value = "Clean local database", group = "Configuration")
    public void cleanStore() {
        this.store.purge();
        // Have to leave the shell
        System.exit(0);
    }

    @ShellMethod(key = "clean archive", value = "Clean archives", group = "Configuration")
    public void cleanArchives() throws IOException {
        Path basePath = Constants.getRootPath();
        Path archivesPath = basePath.resolve(Constants.ARCHIVES);
        Files.deleteIfExists(archivesPath);
    }

    @ShellMethod(key = "password", value = "Change root password", group = "Configuration")
    public void change() {
        String actualPassword = this.prompter.promptForRootPassword();
        String newPassword = this.prompter.doublePromptForPassword();
        this.store.changePassword(actualPassword, newPassword);
    }
}
