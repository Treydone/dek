package fr.layer4.dek.store;

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

import fr.layer4.dek.Constants;
import org.h2.tools.DeleteDbFiles;
import org.junit.After;
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
public class LocalSecuredStoreTest {

    private LocalSecuredStore localSecuredStore;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Before
    public void beforeEachTest() {
        DeleteDbFiles.execute(Constants.getRootPath().toAbsolutePath().toString(), LocalSecuredStore.DB, true);
        this.localSecuredStore = new LocalSecuredStore(applicationEventPublisher);
    }

    @After
    public void afterEachTest() {
        this.localSecuredStore.destroy();
        Mockito.verifyNoMoreInteractions(this.applicationEventPublisher);
        Mockito.reset(this.applicationEventPublisher);
    }

    @Test
    public void startOnEmptyDatabase() {

        // Given

        // When
        this.localSecuredStore.afterPropertiesSet();

        // Then
        assertFalse(this.localSecuredStore.isReady());

    }

    @Test
    public void startOnExistingUnlockedDatabase() {

        // Given
        this.localSecuredStore.afterPropertiesSet();
        this.localSecuredStore.init("le_passsword");
        this.localSecuredStore.destroy();
        Mockito.verify(this.applicationEventPublisher).publishEvent(Mockito.any());

        // When
        this.localSecuredStore.afterPropertiesSet();

        // Then
        assertTrue(this.localSecuredStore.isReady());

    }

    @Test
    public void startOnExistingLockedDatabase() {

        // Given
        this.localSecuredStore.afterPropertiesSet();
        this.localSecuredStore.init("le_password");
        this.localSecuredStore.destroy();
        Mockito.verify(this.applicationEventPublisher).publishEvent(Mockito.any());

        // When
        this.localSecuredStore.afterPropertiesSet();
        this.localSecuredStore.unlock("le_password");

        // Then
        assertTrue(this.localSecuredStore.isReady());

    }

    @Test(expected = RuntimeException.class)
    public void startOnExistingLockedDatabase_wrongPassword() {

        // Given
        this.localSecuredStore.afterPropertiesSet();
        this.localSecuredStore.init("le_password");
        LocalRegistryConnectionManager.updateDdl(this.localSecuredStore.getJdbcTemplate()); // Seems like we need to create a table first to encrypt the whole db...
        this.localSecuredStore.destroy();
        Mockito.verify(this.applicationEventPublisher).publishEvent(Mockito.any());

        // When
        this.localSecuredStore.afterPropertiesSet();
        this.localSecuredStore.unlock("wtf?");

        // Then

    }

    @Test
    public void changePassword() {

        // Given
        this.localSecuredStore.afterPropertiesSet();
        this.localSecuredStore.init("le_password");
        this.localSecuredStore.destroy();
        Mockito.verify(this.applicationEventPublisher).publishEvent(Mockito.any());

        // When
        this.localSecuredStore.afterPropertiesSet();
        this.localSecuredStore.unlock("le_password");
        this.localSecuredStore.changePassword("le_password", "new_password");
        assertTrue(this.localSecuredStore.isReady());

        // Then
        this.localSecuredStore.destroy();
        this.localSecuredStore.unlock("new_password");
        assertTrue(this.localSecuredStore.isReady());
    }
}
