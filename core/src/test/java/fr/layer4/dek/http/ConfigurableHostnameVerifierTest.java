package fr.layer4.dek.http;

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

import fr.layer4.dek.PropertyManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.net.ssl.HostnameVerifier;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurableHostnameVerifierTest {

    @Mock
    private PropertyManager propertyManager;

    @Mock
    private HostnameVerifier defaultHostnameVerifier;

    private ConfigurableHostnameVerifier configurableHostnameVerifier;

    @Before
    public void beforeEachTest() {
        this.configurableHostnameVerifier = new ConfigurableHostnameVerifier(propertyManager, defaultHostnameVerifier);
    }

    @After
    public void afterEachTest() {
        Mockito.verifyNoMoreInteractions(this.propertyManager, this.defaultHostnameVerifier);
        Mockito.reset(this.propertyManager, this.defaultHostnameVerifier);
    }

    @Test
    public void defaultConf() {

        // Given
        Mockito.when(this.propertyManager.getProperty("http.insecure")).thenReturn(Optional.empty());
        Mockito.when(this.defaultHostnameVerifier.verify("le_host", null)).thenReturn(false);

        // When
        boolean verify = this.configurableHostnameVerifier.verify("le_host", null);

        // Then
        assertThat(verify).isFalse();
        Mockito.verify(propertyManager).getProperty("http.insecure");
        Mockito.verify(this.defaultHostnameVerifier).verify("le_host", null);
    }

    @Test
    public void forcedToFalse() {

        // Given
        Mockito.when(this.propertyManager.getProperty("http.insecure")).thenReturn(Optional.of("false"));
        Mockito.when(this.defaultHostnameVerifier.verify("le_host", null)).thenReturn(false);

        // When
        boolean verify = this.configurableHostnameVerifier.verify("le_host", null);

        // Then
        assertThat(verify).isFalse();
        Mockito.verify(propertyManager).getProperty("http.insecure");
        Mockito.verify(this.defaultHostnameVerifier).verify("le_host", null);
    }

    @Test
    public void forcedToTrue() {

        // Given
        Mockito.when(this.propertyManager.getProperty("http.insecure")).thenReturn(Optional.of("true"));

        // When
        boolean verify = this.configurableHostnameVerifier.verify("le_host", null);

        // Then
        assertThat(verify).isTrue();
        Mockito.verify(propertyManager).getProperty("http.insecure");
    }
}
