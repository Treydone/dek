package fr.layer4.hhsl.http;

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

import fr.layer4.hhsl.PropertyManager;
import org.apache.http.client.methods.HttpGet;
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
public class ProxyInterceptorTest {

    @Mock
    private PropertyManager propertyManager;

    private ProxyInterceptor proxyInterceptor;

    @Before
    public void beforeEachTest() {
        this.proxyInterceptor = new ProxyInterceptor(propertyManager);
    }

    @After
    public void afterEachTest() {
        Mockito.verifyNoMoreInteractions(this.propertyManager);
        Mockito.reset(this.propertyManager);
    }

    @Test
    public void defaultConf() {

        // Given
        Mockito.when(propertyManager.getProperty("http.socket.timeout")).thenReturn(Optional.empty());
        Mockito.when(propertyManager.getProperty("http.connect.timeout")).thenReturn(Optional.empty());
        Mockito.when(propertyManager.getProperty("proxy.enabled")).thenReturn(Optional.empty());
        HttpGet httpRequest = new HttpGet("");

        // When
        this.proxyInterceptor.process(httpRequest, null);

        // Then
        assertThat(httpRequest.getConfig().getProxy()).isNull();
        assertThat(httpRequest.getConfig().getSocketTimeout()).isEqualTo(30000);
        assertThat(httpRequest.getConfig().getConnectTimeout()).isEqualTo(30000);

        Mockito.verify(propertyManager).getProperty("http.socket.timeout");
        Mockito.verify(propertyManager).getProperty("http.connect.timeout");
        Mockito.verify(propertyManager).getProperty("proxy.enabled");
    }

    @Test
    public void timeout() {

        // Given
        Mockito.when(propertyManager.getProperty("http.socket.timeout")).thenReturn(Optional.of("1000"));
        Mockito.when(propertyManager.getProperty("http.connect.timeout")).thenReturn(Optional.of("1500"));
        Mockito.when(propertyManager.getProperty("proxy.enabled")).thenReturn(Optional.empty());
        HttpGet httpRequest = new HttpGet("");

        // When
        this.proxyInterceptor.process(httpRequest, null);

        // Then
        assertThat(httpRequest.getConfig().getProxy()).isNull();
        assertThat(httpRequest.getConfig().getSocketTimeout()).isEqualTo(1000);
        assertThat(httpRequest.getConfig().getConnectTimeout()).isEqualTo(1500);

        Mockito.verify(propertyManager).getProperty("http.socket.timeout");
        Mockito.verify(propertyManager).getProperty("http.connect.timeout");
        Mockito.verify(propertyManager).getProperty("proxy.enabled");
    }
}
