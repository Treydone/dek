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
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IntegrationTest {

    @Ignore
    @Test
    public void test() throws IOException {

        Map<String, String> properties = new HashMap<>();
        PropertyManager propertyManager = new PropertyManager() {
            @Override
            public Map<String, String> getProperty() {
                return properties;
            }

            @Override
            public Optional<String> getProperty(String key) {
                return properties.get(key) != null ? Optional.of(properties.get(key)) : Optional.empty();
            }

            @Override
            public void setProperty(String key, String value) {
                properties.put(key, value);
            }

            @Override
            public void deleteProperty(String key) {
                properties.remove(key);
            }
        };

        propertyManager.setProperty(HttpProperties.PROXY_ENABLED, "true");
        propertyManager.setProperty(HttpProperties.PROXY_HOST, "www.my-proxy.com");
        propertyManager.setProperty(HttpProperties.PROXY_PORT, "80");
        propertyManager.setProperty(HttpProperties.PROXY_AUTH_TYPE, HttpProperties.PROXY_AUTH_BASIC);
        propertyManager.setProperty(HttpProperties.PROXY_AUTH_BASIC_USER, "le_user");
        propertyManager.setProperty(HttpProperties.PROXY_AUTH_BASIC_PASSWORD, "le_password");

        CloseableHttpClient build = HttpClientBuilder.create()
                .setSSLHostnameVerifier(new ConfigurableHostnameVerifier(propertyManager, NoopHostnameVerifier.INSTANCE))
                .setRoutePlanner(new NonProxyRoutePlanner(new DefaultSchemePortResolver(), propertyManager))
                .setDefaultCredentialsProvider(new WithProxyAuthCredentialsProvider(propertyManager))
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setSocketTimeout(Integer.valueOf(propertyManager.getProperty("http.socket.timeout").orElse("30000")))
                                .setConnectTimeout(Integer.valueOf(propertyManager.getProperty("http.connect.timeout").orElse("30000")))
                                .setProxyPreferredAuthSchemes(Arrays.asList("basic", "ntlm"))
                                .build()
                ).setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy())
                .build();

        build.execute(new HttpGet("http://google.fr"));
    }
}
