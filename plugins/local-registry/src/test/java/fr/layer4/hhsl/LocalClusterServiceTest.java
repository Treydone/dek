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
import fr.layer4.hhsl.store.LocalLockableStore;
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
public class LocalClusterServiceTest {

    @Mock
    private LocalLockableStore localLockableStore;

    @Mock
    private Prompter prompter;

    private LocalClusterService localClusterService;
    private JdbcConnectionPool pool;


    @Before
    public void beforeEachTest() {
        this.pool = JdbcConnectionPool.create("jdbc:h2:mem:db", "sa", "sa");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(this.pool);
        Mockito.when(this.localLockableStore.getJdbcTemplate()).thenReturn(jdbcTemplate);
        this.localClusterService = new LocalClusterService(this.localLockableStore, this.prompter);
        LocalClusterService.updateDdl(jdbcTemplate);
    }

    @After
    public void afterEachTest() {
        this.pool.dispose();
        Mockito.verify(this.localLockableStore, Mockito.atLeast(1)).getJdbcTemplate();
        Mockito.verifyNoMoreInteractions(this.prompter, this.localLockableStore);
        Mockito.reset(this.prompter, this.localLockableStore);
    }

    @Test
    public void getCluster_ok() {

        // Given
        Mockito.when(this.prompter.prompt("User:")).thenReturn("le_user");
        Mockito.when(this.prompter.promptForPassword("Password:")).thenReturn("le_password");
        this.localClusterService.addOrUpdateCluster("type", "test", "file:///1", "banner");
        this.localClusterService.addOrUpdateCluster("type", "name2", "file:///2", "banner");
        Cluster expected = new Cluster();
        expected.setType("type");
        expected.setName("test");
        expected.setUri(URI.create("file:///1"));
        expected.setBanner("banner".getBytes());
        expected.setRegistry("local");
        expected.setUser("le_user");
        expected.setPassword("le_password");

        // When
        Optional<Cluster> cluster = this.localClusterService.getCluster("test");

        // Then
        assertThat(cluster).isPresent().get().isEqualToIgnoringGivenFields(expected, "id");
        Mockito.verify(prompter, Mockito.times(2)).prompt("User:");
        Mockito.verify(prompter, Mockito.times(2)).promptForPassword("Password:");
    }

    @Test
    public void getCluster_unknown() {

        // Given

        // When
        Optional<Cluster> cluster = this.localClusterService.getCluster("test");

        // Then
        assertThat(cluster).isNotPresent();

    }

    @Test
    public void addOrUpdateCluster_ok() {

        // Given
        Mockito.when(this.prompter.prompt("User:")).thenReturn("le_user");
        Mockito.when(this.prompter.promptForPassword("Password:")).thenReturn("le_password");

        // When
        Cluster cluster = this.localClusterService.addOrUpdateCluster("type", "name", "uri", "banner");

        // Then
        assertThat(cluster.getId()).isNotNull().isGreaterThan(0L);
        Mockito.verify(prompter, Mockito.times(1)).prompt("User:");
        Mockito.verify(prompter, Mockito.times(1)).promptForPassword("Password:");
    }

    @Test
    public void deleteCluster_unknownCluster() {

        // Given
        Mockito.when(this.prompter.prompt("User:")).thenReturn("le_user");
        Mockito.when(this.prompter.promptForPassword("Password:")).thenReturn("le_password");
        this.localClusterService.addOrUpdateCluster("type", "test", "file:///1", "banner");
        this.localClusterService.addOrUpdateCluster("type", "test2", "file:///2", "banner");

        // When
        this.localClusterService.deleteCluster("test3");

        // Then
        List<Cluster> clusters = this.localClusterService.listClusters();
        assertThat(clusters).isNotEmpty().hasSize(2);
        Mockito.verify(prompter, Mockito.times(2)).prompt("User:");
        Mockito.verify(prompter, Mockito.times(2)).promptForPassword("Password:");
    }

    @Test
    public void deleteCluster_oneCluster() {

        // Given
        Mockito.when(this.prompter.prompt("User:")).thenReturn("le_user");
        Mockito.when(this.prompter.promptForPassword("Password:")).thenReturn("le_password");
        this.localClusterService.addOrUpdateCluster("type", "test", "file:///1", "banner");
        this.localClusterService.addOrUpdateCluster("type", "test2", "file:///2", "banner");
        Cluster expected = new Cluster();
        expected.setType("type");
        expected.setName("test");
        expected.setUri(URI.create("file:///1"));
        expected.setBanner("banner".getBytes());
        expected.setRegistry("local");
        expected.setUser("le_user");
        expected.setPassword("le_password");

        // When
        this.localClusterService.deleteCluster("test2");

        // Then
        List<Cluster> clusters = this.localClusterService.listClusters();
        assertThat(clusters).isNotEmpty().hasSize(1);
        assertThat(clusters.get(0)).isEqualToIgnoringGivenFields(expected, "id");
        Mockito.verify(prompter, Mockito.times(2)).prompt("User:");
        Mockito.verify(prompter, Mockito.times(2)).promptForPassword("Password:");
    }
}
