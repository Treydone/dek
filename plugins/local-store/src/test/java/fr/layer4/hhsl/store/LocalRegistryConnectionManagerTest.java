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

import fr.layer4.hhsl.registry.RegistryConnection;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LocalRegistryConnectionManagerTest {

    @Mock
    private LocalLockableStore localLockableStore;

    private LocalRegistryConnectionManager registryConnectionManager;
    private JdbcConnectionPool pool;

    @Before
    public void beforeEachTest() {
        this.pool = JdbcConnectionPool.create("jdbc:h2:mem:db", "sa", "sa");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(this.pool);
        Mockito.when(this.localLockableStore.getJdbcTemplate()).thenReturn(jdbcTemplate);
        this.registryConnectionManager = new LocalRegistryConnectionManager(this.localLockableStore);
        LocalRegistryConnectionManager.updateDdl(jdbcTemplate);
    }

    @After
    public void afterEachTest() {
        this.pool.dispose();
        Mockito.verify(this.localLockableStore, Mockito.atLeast(1)).getJdbcTemplate();
        Mockito.verifyNoMoreInteractions(this.localLockableStore);
        Mockito.reset(this.localLockableStore);
    }

    @Test
    public void listRegistries_empty() {

        // Given

        // When
        List<RegistryConnection> connections = this.registryConnectionManager.listRegistries();

        // Then
        assertThat(connections).isEmpty();

    }

    @Test
    public void listRegistries_oneRegistry() {

        // Given
        this.registryConnectionManager.addOrUpdateRegistry("pouet", "file:///");
        RegistryConnection expected = new RegistryConnection();
        expected.setName("pouet");
        expected.setUri(URI.create("file:///"));

        // When
        List<RegistryConnection> connections = this.registryConnectionManager.listRegistries();

        // Then
        assertThat(connections).isNotEmpty().hasSize(1);
        assertThat(connections.get(0)).isEqualToIgnoringGivenFields(expected, "id");

    }

    @Test
    public void deleteRegistry_oneRegistry() {

        // Given
        this.registryConnectionManager.addOrUpdateRegistry("pouet", "file:///");
        this.registryConnectionManager.addOrUpdateRegistry("pouet2", "file:///");
        RegistryConnection expected = new RegistryConnection();
        expected.setName("pouet2");
        expected.setUri(URI.create("file:///"));

        // When
        this.registryConnectionManager.deleteRegistry("pouet");

        // Then
        List<RegistryConnection> connections = this.registryConnectionManager.listRegistries();
        assertThat(connections).isNotEmpty().hasSize(1);
        assertThat(connections.get(0)).isEqualToIgnoringGivenFields(expected, "id");

    }

    @Test
    public void deleteRegistry_unknownRegistry() {

        // Given
        this.registryConnectionManager.addOrUpdateRegistry("pouet", "file:///");
        this.registryConnectionManager.addOrUpdateRegistry("pouet2", "file:///");

        // When
        this.registryConnectionManager.deleteRegistry("pouet3");

        // Then
        List<RegistryConnection> connections = this.registryConnectionManager.listRegistries();
        assertThat(connections).isNotEmpty().hasSize(2);

    }

    @Test
    public void getRegistry_found() {

        // Given
        this.registryConnectionManager.addOrUpdateRegistry("pouet", "file:///");
        this.registryConnectionManager.addOrUpdateRegistry("pouet2", "file:///");
        RegistryConnection expected = new RegistryConnection();
        expected.setName("pouet");
        expected.setUri(URI.create("file:///"));

        // When
        Optional<RegistryConnection> registry = this.registryConnectionManager.getRegistry("pouet");

        // Then
        assertThat(registry).isPresent().get().isEqualToIgnoringGivenFields(expected, "id");

    }

    @Test
    public void getRegistry_notFound() {

        // Given
        this.registryConnectionManager.addOrUpdateRegistry("pouet", "file:///");
        this.registryConnectionManager.addOrUpdateRegistry("pouet2", "file:///");

        // When
        Optional<RegistryConnection> registry = this.registryConnectionManager.getRegistry("pouet3");

        // Then
        assertThat(registry).isNotPresent();

    }
}
