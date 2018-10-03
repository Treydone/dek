package fr.layer4.hhsl.local;

import fr.layer4.hhsl.Utils;
import org.h2.tools.DeleteDbFiles;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import static fr.layer4.hhsl.local.H2LocalStore.DB;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class H2LocalStoreTest {

    private H2LocalStore h2LocalStore;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private Utils utils;

    @Before
    public void beforeEachTest() {
        DeleteDbFiles.execute(H2LocalStore.getRootPath(), DB, true);
        h2LocalStore = new H2LocalStore();
        h2LocalStore.setUtils(utils);
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
        Mockito.when(utils.doublePromptForPassword()).thenReturn("le_password");

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
        Mockito.when(utils.doublePromptForPassword()).thenReturn("le_password");

        h2LocalStore.afterPropertiesSet();
        h2LocalStore.init(true);
        h2LocalStore.destroy();

        // When
        h2LocalStore.afterPropertiesSet();
        Mockito.when(utils.promptForPassword()).thenReturn("le_password");
        h2LocalStore.unlock();

        // Then
        assertTrue(h2LocalStore.isReady());
        assertTrue(h2LocalStore.isUnlocked());

    }
}
