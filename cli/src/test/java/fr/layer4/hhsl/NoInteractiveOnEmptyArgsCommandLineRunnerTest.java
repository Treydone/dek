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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.shell.Shell;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.fail;

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

    private NoInteractiveOnEmptyArgsCommandLineRunner noInteractiveOnEmptyArgsCommandLineRunner;

    @Before
    public void beforeEachTest() {
        this.noInteractiveOnEmptyArgsCommandLineRunner = new NoInteractiveOnEmptyArgsCommandLineRunner(this.shell, this.environment, this.store, this.prompter);
    }

    @After
    public void afterEachTest() {
        Mockito.verifyNoMoreInteractions(this.shell, this.environment, this.store, this.prompter);
        Mockito.reset(this.shell, this.environment, this.store, this.prompter);
    }

    @Test
    public void storeNotReady() throws Exception {

        // Given
        Mockito.when(this.store.isReady()).thenReturn(false);
        Mockito.when(this.prompter.doublePromptForPassword()).thenReturn("le_password");

        // When
        this.noInteractiveOnEmptyArgsCommandLineRunner.run();

        // Then
        Mockito.verify(this.store).isReady();
        Mockito.verify(this.prompter).doublePromptForPassword();
        Mockito.verify(this.store).init("le_password");
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
}
