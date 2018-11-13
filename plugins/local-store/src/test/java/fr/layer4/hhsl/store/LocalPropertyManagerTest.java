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

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LocalPropertyManagerTest {

    @Mock
    private LocalSecuredStore localSecuredStore;

    private LocalPropertyManager propertyManager;
    private JdbcConnectionPool pool;

    @Before
    public void beforeEachTest() {
        this.pool = JdbcConnectionPool.create("jdbc:h2:mem:db", "sa", "sa");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(this.pool);
        Mockito.when(this.localSecuredStore.getJdbcTemplate()).thenReturn(jdbcTemplate);
        this.propertyManager = new LocalPropertyManager(this.localSecuredStore);
        LocalPropertyManager.updateDdl(jdbcTemplate);
    }

    @After
    public void afterEachTest() {
        this.pool.dispose();
        Mockito.verify(this.localSecuredStore, Mockito.atLeast(1)).getJdbcTemplate();
        Mockito.verifyNoMoreInteractions(this.localSecuredStore);
        Mockito.reset(this.localSecuredStore);
    }

    @Test
    public void setProperty() {

        // Given

        // When
        this.propertyManager.setProperty("test", "toto");

        // Then

    }

    @Test
    public void setProperty_multipleAndOverriding() {

        // Given

        // When
        this.propertyManager.setProperty("test", "toto");
        this.propertyManager.setProperty("test", "titi");
        this.propertyManager.setProperty("test2", "tata");

        // Then
        assertThat(this.propertyManager.getProperty("test")).isPresent().isNotEmpty().get().isEqualTo("titi");
    }

    @Test
    public void deleteProperty_unknownProperty() {

        // Given

        // When
        this.propertyManager.deleteProperty("test");

        // Then
        assertThat(this.propertyManager.getProperty("test")).isNotPresent();
    }

    @Test
    public void deleteProperty_knownProperty() {

        // Given
        this.propertyManager.setProperty("test", "toto");

        // When
        this.propertyManager.deleteProperty("test");

        // Then
        assertThat(this.propertyManager.getProperty("test")).isNotPresent();
    }

    @Test
    public void getProperty_noResult() {

        // Given

        // When
        Optional<String> property = this.propertyManager.getProperty("test");

        // Then
        assertThat(property).isNotPresent();

    }

    @Test
    public void getProperty_withResult() {

        // Given
        this.propertyManager.setProperty("test", "toto");

        // When
        Optional<String> property = this.propertyManager.getProperty("test");

        // Then
        assertThat(property).isPresent().isNotEmpty().get().isEqualTo("toto");

    }

    @Test
    public void getProperty() {

        // Given
        this.propertyManager.setProperty("test", "toto");
        this.propertyManager.setProperty("test", "titi");
        this.propertyManager.setProperty("test2", "tata");
        this.propertyManager.deleteProperty("test2");

        // When
        Map<String, String> properties = this.propertyManager.getProperty();

        // Then
        assertThat(properties).hasSize(1).containsEntry("test", "titi");
    }

    @Test
    public void getProperty_default() {

        // Given
        LocalPropertyManager.updateData(this.localSecuredStore.getJdbcTemplate());

        // When

        // Then
        assertThat(this.propertyManager.getProperty("http.socket.timeout")).isPresent().isNotEmpty().get().isEqualTo("30000");
        assertThat(this.propertyManager.getProperty("http.connect.timeout")).isPresent().isNotEmpty().get().isEqualTo("30000");
        assertThat(this.propertyManager.getProperty("proxy.enabled")).isPresent().isNotEmpty().get().isEqualTo("false");

    }
}
