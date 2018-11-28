package fr.layer4.dek;

/*-
 * #%L
 * DEK
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

import fr.layer4.dek.prompt.Prompter;
import fr.layer4.dek.store.SecuredStore;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.shell.Input;
import org.springframework.shell.Shell;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.argThat;
import static org.springframework.shell.jline.InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class NoInteractiveOnEmptyArgsCommandLineRunnerTest {

    @Mock
    private Shell shell;

    @Mock
    private ConfigurableEnvironment environment;

    @Mock
    private SecuredStore store;

    @Mock
    private Prompter prompter;

    @Mock
    private MutablePropertySources propertySources;

    @Mock
    private ConfigurableApplicationContext context;

    private NoInteractiveOnEmptyArgsCommandLineRunner noInteractiveOnEmptyArgsCommandLineRunner;

    @Before
    public void beforeEachTest() throws IOException {
        this.noInteractiveOnEmptyArgsCommandLineRunner = new NoInteractiveOnEmptyArgsCommandLineRunner(this.shell, this.environment, this.store, this.prompter, context);
        Mockito.when(this.environment.getPropertySources()).thenReturn(this.propertySources);
        Files.deleteIfExists(Constants.getRootPath().resolve(Constants.SECRET));
    }

    @After
    public void afterEachTest() {
        Mockito.verifyNoMoreInteractions(this.shell, this.environment, this.store, this.prompter, this.propertySources, this.context);
        Mockito.reset(this.shell, this.environment, this.store, this.prompter, this.propertySources, this.context);
    }

    @Test
    public void storeNotReady() throws Exception {

        // Given
        Mockito.when(this.store.isReady()).thenReturn(false);

        try {
            this.noInteractiveOnEmptyArgsCommandLineRunner.run();
            fail();
        } catch (RuntimeException e) {

        }

        // Then
        Mockito.verify(this.store).isReady();
    }

    @Test
    public void storeReady_noUnlockOption() throws Exception {

        // Given
        Mockito.when(this.store.isReady()).thenReturn(true);

        // When
        try {
            this.noInteractiveOnEmptyArgsCommandLineRunner.run();
            fail();
        } catch (RuntimeException e) {

        }

        // Then
        Mockito.verify(this.store).isReady();
    }

    @Test
    public void info() throws Exception {

        // Given
        Mockito.when(this.store.isReady()).thenReturn(true);

        // When
        this.noInteractiveOnEmptyArgsCommandLineRunner.run("info", "--unlock", "password");

        // Then
        Mockito.verify(this.store).isReady();
        Mockito.verify(this.store).unlock("password");
        Mockito.verify(this.context).getBeansOfType(ExitCodeGenerator.class);
        Mockito.verify(this.context).close();
        verifyExecution("info");

    }

    protected void verifyExecution(String... args) throws IOException {
        Mockito.verify(this.environment).getPropertySources();
        Mockito.verify(this.propertySources).addFirst(new MapPropertySource("interactive.override", Collections.singletonMap(SPRING_SHELL_INTERACTIVE_ENABLED, "false")));
        Mockito.verify(this.shell).run(argThat(inputProvider -> {
            Input input = inputProvider.readInput();
            if (input != null) {
                try {
                    assertThat(input.words()).containsExactly(args);
                    return true;
                } catch (Exception e) {
                    log.error("Nope...", e);
                    return false;
                }
            }
            return true;
        }));
    }

    @Test
    public void storeReady_unlockWithPassword() throws Exception {

        // Given
        Mockito.when(this.store.isReady()).thenReturn(true);

        // When
        this.noInteractiveOnEmptyArgsCommandLineRunner.run("--unlock", "le_password");

        // Then
        Mockito.verify(this.store).isReady();
        Mockito.verify(this.store).unlock("le_password");
    }

    @Test
    public void storeReady_unlockWithPrompt() throws Exception {

        // Given
        Mockito.when(this.store.isReady()).thenReturn(true);
        Mockito.when(this.prompter.promptForPassword("Password:")).thenReturn("some password");

        // When
        this.noInteractiveOnEmptyArgsCommandLineRunner.run("--unlock", "@prompt");

        // Then
        Mockito.verify(this.store).isReady();
        Mockito.verify(this.prompter).promptForPassword("Password:");
        Mockito.verify(this.store).unlock("some password");
    }

    @Test
    public void storeReady_unlockWithFile() throws Exception {

        // Given
        Mockito.when(this.store.isReady()).thenReturn(true);

        Path tempFile = Files.createTempFile("test", "template");
        Files.write(tempFile, "password in file".getBytes());
        String path = tempFile.toAbsolutePath().toString();

        // When
        this.noInteractiveOnEmptyArgsCommandLineRunner.run("--unlock", "file://" + path);

        // Then
        Mockito.verify(this.store).isReady();
        Mockito.verify(this.store).unlock("password in file");
    }

    @Test
    public void storeReady_unlockWithDefaultSecretFile() throws Exception {

        // Given
        Mockito.when(this.store.isReady()).thenReturn(true);
        Files.write(Constants.getRootPath().resolve(Constants.SECRET), "password in secret file".getBytes());

        // When
        this.noInteractiveOnEmptyArgsCommandLineRunner.run();

        // Then
        Mockito.verify(this.store).isReady();
        Mockito.verify(this.store).unlock("password in secret file");
    }
}
