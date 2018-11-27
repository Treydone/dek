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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.layer4.dek.auth.Credentials;
import fr.layer4.dek.store.LocalSecuredStore;
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
    private LocalSecuredStore localSecuredStore;

    private LocalClusterService localClusterService;
    private JdbcConnectionPool pool;


    @Before
    public void beforeEachTest() {
        this.pool = JdbcConnectionPool.create("jdbc:h2:mem:db", "sa", "sa");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(this.pool);
        Mockito.when(this.localSecuredStore.getJdbcTemplate()).thenReturn(jdbcTemplate);
        this.localClusterService = new LocalClusterService(this.localSecuredStore, new ObjectMapper());
        LocalClusterService.updateDdl(jdbcTemplate);
    }

    @After
    public void afterEachTest() {
        this.pool.dispose();
        Mockito.verify(this.localSecuredStore, Mockito.atLeast(1)).getJdbcTemplate();
        Mockito.verifyNoMoreInteractions(this.localSecuredStore);
        Mockito.reset(this.localSecuredStore);
    }

    @Test
    public void getCluster_ok() {

        // Given
        this.localClusterService.addOrUpdateCluster("type", "test", "file:///1", "banner", Credentials.basic("le_user", "le_password"));
        this.localClusterService.addOrUpdateCluster("type", "name2", "file:///2", "banner", Credentials.basic("le_user", "le_password"));
        Cluster expected = new Cluster();
        expected.setType("type");
        expected.setName("test");
        expected.setUri(URI.create("file:///1"));
        expected.setBanner("banner".getBytes());
        expected.setRegistry("local");
        expected.setCredentials(Credentials.basic("le_user", "le_password"));

        // When
        Optional<Cluster> cluster = this.localClusterService.getCluster("test");

        // Then
        assertThat(cluster).isPresent().get().isEqualToIgnoringGivenFields(expected, "id");
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

        // When
        Cluster cluster = this.localClusterService.addOrUpdateCluster("type", "name", "uri", "banner", Credentials.basic("le_user", "le_password"));

        // Then
        assertThat(cluster.getId()).isNotNull().isGreaterThan(0L);
    }

    @Test
    public void deleteCluster_unknownCluster() {

        // Given
        this.localClusterService.addOrUpdateCluster("type", "test", "file:///1", "banner", Credentials.basic("le_user", "le_password"));
        this.localClusterService.addOrUpdateCluster("type", "test2", "file:///2", "banner", Credentials.basic("le_user", "le_password"));

        // When
        this.localClusterService.deleteCluster("test3");

        // Then
        List<Cluster> clusters = this.localClusterService.listClusters();
        assertThat(clusters).isNotEmpty().hasSize(2);
    }

    @Test
    public void deleteCluster_oneCluster() {

        // Given
        this.localClusterService.addOrUpdateCluster("type", "test", "file:///1", "banner", Credentials.basic("le_user", "le_password"));
        this.localClusterService.addOrUpdateCluster("type", "test2", "file:///2", "banner", Credentials.basic("le_user", "le_password"));
        Cluster expected = new Cluster();
        expected.setType("type");
        expected.setName("test");
        expected.setUri(URI.create("file:///1"));
        expected.setBanner("banner".getBytes());
        expected.setRegistry("local");
        expected.setCredentials(Credentials.basic("le_user", "le_password"));

        // When
        this.localClusterService.deleteCluster("test2");

        // Then
        List<Cluster> clusters = this.localClusterService.listClusters();
        assertThat(clusters).isNotEmpty().hasSize(1);
        assertThat(clusters.get(0)).isEqualToIgnoringGivenFields(expected, "id");
    }
}
