package fr.layer4.hhsl.store;

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
import org.h2.tools.DeleteDbFiles;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class LockableLocalStoreTest {

    private LockableLocalStore h2LocalStore;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private Prompter prompter;

    @Before
    public void beforeEachTest() {
        DeleteDbFiles.execute(LocalStoreConstants.getRootPath(), LockableLocalStore.DB, true);
        h2LocalStore = new LockableLocalStore();
        h2LocalStore.setPrompter(prompter);
        h2LocalStore.setApplicationEventPublisher(eventPublisher);
    }

    @Test
    public void startOnEmptyDatabase() {

        // Given

        // When
        h2LocalStore.afterPropertiesSet();

        // Then
        assertFalse(h2LocalStore.isReady());
        assertFalse(h2LocalStore.isUnlocked());

    }

    @Test
    public void startOnExistingUnlockedDatabase() {

        // Given
        h2LocalStore.afterPropertiesSet();
        h2LocalStore.init(false);
        h2LocalStore.destroy();

        // When
        h2LocalStore.afterPropertiesSet();

        // Then
        assertTrue(h2LocalStore.isReady());
        assertTrue(h2LocalStore.isUnlocked());

    }

    @Test
    public void startOnExistingLockedDatabase() {

        // Given
        Mockito.when(prompter.doublePromptForPassword()).thenReturn("le_password");

        h2LocalStore.afterPropertiesSet();
        h2LocalStore.init(true);
        h2LocalStore.destroy();

        // When
        h2LocalStore.afterPropertiesSet();

        // Then
        assertTrue(h2LocalStore.isReady());
        assertFalse(h2LocalStore.isUnlocked());

    }

    @Test
    public void unlockExistingLockedDatabase() {

        // Given
        Mockito.when(prompter.doublePromptForPassword()).thenReturn("le_password");

        h2LocalStore.afterPropertiesSet();
        h2LocalStore.init(true);
        h2LocalStore.destroy();

        // When
        h2LocalStore.afterPropertiesSet();
        Mockito.when(prompter.promptForRootPassword()).thenReturn("le_password");
        h2LocalStore.unlock();

        // Then
        assertTrue(h2LocalStore.isReady());
        assertTrue(h2LocalStore.isUnlocked());

    }
}
