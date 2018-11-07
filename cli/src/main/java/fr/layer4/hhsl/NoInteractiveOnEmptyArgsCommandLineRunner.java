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

import fr.layer4.hhsl.store.LockableStore;
import fr.layer4.hhsl.store.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.shell.Availability;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.stereotype.Component;

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
    private final Store store;

    @Override
    public void run(String... args) throws Exception {
        List<String> commandsToRun = Arrays.stream(args)
                .filter(w -> !w.startsWith("@"))
                .collect(Collectors.toList());

        int i = commandsToRun.indexOf("--no-prompt");
        if (i > -1) {
            if (i + 1 > commandsToRun.size()) {
                log.error("Missing password");
                System.exit(1);
            }
        }

//        if (store instanceof LockableStore) {
//            LockableStore lockableStore = (LockableStore) store;
//            if (!lockableStore.isUnlocked()) {
//
//                lockableStore.unlock();
//            }
//        }

        if (!commandsToRun.isEmpty()) {
            InteractiveShellApplicationRunner.disable(environment);
            shell.run(new StringInputProvider(commandsToRun));
        }
    }
}
