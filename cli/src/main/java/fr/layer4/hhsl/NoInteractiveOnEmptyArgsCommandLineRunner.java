package fr.layer4.hhsl;

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

import fr.layer4.hhsl.prompt.Prompter;
import fr.layer4.hhsl.store.SecuredStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bypass InteractiveShellApplicationRunner if commmandline arguments are present.
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Order(InteractiveShellApplicationRunner.PRECEDENCE - 2)
class NoInteractiveOnEmptyArgsCommandLineRunner implements CommandLineRunner {

    private final Shell shell;
    private final ConfigurableEnvironment environment;
    private final SecuredStore store;
    private final Prompter prompter;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(String... args) throws Exception {
        List<String> commandsToRun = Arrays.stream(args).collect(Collectors.toList());

        if (commandsToRun.contains("init")) {
            if (commandsToRun.size() > 1) {
                throw new RuntimeException("'init' doesn't accept parameter");
            } else {
                // Init the store, and purge before if necessary, then stop
                String password = this.prompter.doublePromptForPassword();
                this.store.init(password);
                System.exit(SpringApplication.exit(context));
            }
        }

        if (!this.store.isReady()) {
            throw new RuntimeException("Store not ready, please run 'hhsl init' before");
        }

        int i = commandsToRun.indexOf("--unlock");
        if (i > -1) {
            if (i + 1 > commandsToRun.size()) {
                throw new RuntimeException("unlock argument without value @prompt|<password>|<file:///.....>");
            }
            commandsToRun.remove(i);
            String option = commandsToRun.remove(i);

            String password;
            if ("@prompt".equalsIgnoreCase(option)) {
                password = this.prompter.promptForPassword("Password:");
            } else if (option.startsWith("file://")) {
                File file = new File(option.replace("file://", ""));
                if (file.exists()) {
                    password = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                } else {
                    throw new RuntimeException("File not found:" + option);
                }
            } else {
                password = option;
            }

            this.store.unlock(password);
        } else {
            throw new RuntimeException("Missing unlock option");
        }


        if (!commandsToRun.isEmpty()) {
            InteractiveShellApplicationRunner.disable(this.environment);
            this.shell.run(new StringInputProvider(commandsToRun));
        }
    }
}
