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

import fr.layer4.dek.property.PropertyManager;
import org.apache.http.HttpHost;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class NonProxyRoutePlannerTest {

    @Mock
    private PropertyManager propertyManager;

    private NonProxyRoutePlanner nonProxyRoutePlanner;

    @Before
    public void beforeEachTest() {
        this.nonProxyRoutePlanner = new NonProxyRoutePlanner(null, this.propertyManager);
    }

    @After
    public void afterEachTest() {
        Mockito.verifyNoMoreInteractions(this.propertyManager);
        Mockito.reset(this.propertyManager);
    }

    @Test
    public void defaultNoProxy() {

        // Given
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_ENABLED)).thenReturn(Optional.empty());
        HttpHost target = new HttpHost("dek.fr");

        // When
        HttpHost proxy = this.nonProxyRoutePlanner.determineProxy(target, null, null);

        // Then
        assertThat(proxy).isNull();
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_ENABLED);
    }

    @Test
    public void excludeHostWithProxy() {

        // Given
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_ENABLED)).thenReturn(Optional.of("true"));
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_NON_PROXY_HOSTS)).thenReturn(Optional.of("dek.fr"));
        HttpHost target = new HttpHost("dek.fr");

        // When
        HttpHost proxy = this.nonProxyRoutePlanner.determineProxy(target, null, null);

        // Then
        assertThat(proxy).isNull();
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_ENABLED);
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_NON_PROXY_HOSTS);
    }

    @Test
    public void justProxy() {

        // Given
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_ENABLED)).thenReturn(Optional.of("true"));
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_NON_PROXY_HOSTS)).thenReturn(Optional.of("localhost"));
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_HOST)).thenReturn(Optional.of("leproxy.fr"));
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_PORT)).thenReturn(Optional.of("546"));
        HttpHost target = new HttpHost("dek.fr");

        // When
        HttpHost proxy = this.nonProxyRoutePlanner.determineProxy(target, null, null);

        // Then
        assertThat(proxy).isNotNull().isEqualTo(new HttpHost("leproxy.fr", 546));
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_ENABLED);
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_NON_PROXY_HOSTS);
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_HOST);
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_PORT);
    }
}
