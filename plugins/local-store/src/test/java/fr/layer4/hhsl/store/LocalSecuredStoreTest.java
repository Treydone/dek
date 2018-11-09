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

import fr.layer4.hhsl.Constants;
import org.h2.tools.DeleteDbFiles;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class LocalSecuredStoreTest {

    private LocalLockableStore localLockableStore;

    @Before
    public void beforeEachTest() {
        DeleteDbFiles.execute(Constants.getRootPath(), LocalLockableStore.DB, true);
        this.localLockableStore = new LocalLockableStore();
    }

    @After
    public void afterEachTest() {
        this.localLockableStore.destroy();
    }

    @Test
    public void startOnEmptyDatabase() {

        // Given

        // When
        this.localLockableStore.afterPropertiesSet();

        // Then
        assertFalse(this.localLockableStore.isReady());

    }

    @Test
    public void startOnExistingUnlockedDatabase() {

        // Given
        this.localLockableStore.afterPropertiesSet();
        this.localLockableStore.init("le_passsword");
        this.localLockableStore.destroy();

        // When
        this.localLockableStore.afterPropertiesSet();

        // Then
        assertTrue(this.localLockableStore.isReady());

    }

    @Test
    public void startOnExistingLockedDatabase() {

        // Given
        this.localLockableStore.afterPropertiesSet();
        this.localLockableStore.init("le_password");
        this.localLockableStore.destroy();

        // When
        this.localLockableStore.afterPropertiesSet();
        this.localLockableStore.unlock("le_password");

        // Then
        assertTrue(this.localLockableStore.isReady());

    }

    @Test(expected = RuntimeException.class)
    public void startOnExistingLockedDatabase_wrongPassword() {

        // Given
        this.localLockableStore.afterPropertiesSet();
        this.localLockableStore.init("le_password");
        this.localLockableStore.destroy();

        // When
        this.localLockableStore.afterPropertiesSet();
        this.localLockableStore.unlock("wtf?");

        // Then

    }

    @Test
    public void changePassword() {

        // Given
        this.localLockableStore.afterPropertiesSet();
        this.localLockableStore.init("le_password");
        this.localLockableStore.destroy();

        // When
        this.localLockableStore.afterPropertiesSet();
        this.localLockableStore.unlock("le_password");
        this.localLockableStore.changePassword("le_password", "new_password");
        assertTrue(this.localLockableStore.isReady());

        // Then
        this.localLockableStore.destroy();
        this.localLockableStore.unlock("new_password");
        assertTrue(this.localLockableStore.isReady());
    }
}
