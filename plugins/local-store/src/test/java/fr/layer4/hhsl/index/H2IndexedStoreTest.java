//package fr.layer4.hhsl.index;
//
//import fr.layer4.hhsl.store.LocalStoreConstants;
//import fr.layer4.hhsl.prompt.Prompter;
//import org.h2.tools.DeleteDbFiles;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.springframework.context.ApplicationEventPublisher;
//
//import static fr.layer4.hhsl.index.H2IndexedStore.DB;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//@RunWith(MockitoJUnitRunner.class)
//public class H2IndexedStoreTest {
//
//    private H2IndexedStore h2LocalStore;
//
//    @Mock
//    private ApplicationEventPublisher eventPublisher;
//
//    @Mock
//    private Prompter prompter;
//
//    @Before
//    public void beforeEachTest() {
//        DeleteDbFiles.execute(LocalStoreConstants.getRootPath(), DB, true);
//        h2LocalStore = new H2IndexedStore();
//        h2LocalStore.setPrompter(prompter);
//        h2LocalStore.setApplicationEventPublisher(eventPublisher);
//    }
//
//    @Test
//    public void startOnEmptyDatabase() {
//
//        // Given
//
//        // When
//        h2LocalStore.afterPropertiesSet();
//
//        // Then
//        assertFalse(h2LocalStore.isReady());
//        assertFalse(h2LocalStore.isUnlocked());
//
//    }
//
//    @Test
//    public void startOnExistingUnlockedDatabase() {
//
//        // Given
//        h2LocalStore.afterPropertiesSet();
//        h2LocalStore.init(false);
//        h2LocalStore.destroy();
//
//        // When
//        h2LocalStore.afterPropertiesSet();
//
//        // Then
//        assertTrue(h2LocalStore.isReady());
//        assertTrue(h2LocalStore.isUnlocked());
//
//    }
//
//    @Test
//    public void startOnExistingLockedDatabase() {
//
//        // Given
//        Mockito.when(prompter.doublePromptForPassword()).thenReturn("le_password");
//
//        h2LocalStore.afterPropertiesSet();
//        h2LocalStore.init(true);
//        h2LocalStore.destroy();
//
//        // When
//        h2LocalStore.afterPropertiesSet();
//
//        // Then
//        assertTrue(h2LocalStore.isReady());
//        assertFalse(h2LocalStore.isUnlocked());
//
//    }
//
//    @Test
//    public void unlockExistingLockedDatabase() {
//
//        // Given
//        Mockito.when(prompter.doublePromptForPassword()).thenReturn("le_password");
//
//        h2LocalStore.afterPropertiesSet();
//        h2LocalStore.init(true);
//        h2LocalStore.destroy();
//
//        // When
//        h2LocalStore.afterPropertiesSet();
//        Mockito.when(prompter.promptForRootPassword()).thenReturn("le_password");
//        h2LocalStore.unlock();
//
//        // Then
//        assertTrue(h2LocalStore.isReady());
//        assertTrue(h2LocalStore.isUnlocked());
//
//    }
//}
