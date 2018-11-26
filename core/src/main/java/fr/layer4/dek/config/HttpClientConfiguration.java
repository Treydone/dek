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
package fr.layer4.dek.config;

import fr.layer4.dek.PropertyManager;
import fr.layer4.dek.http.ConfigurableHostnameVerifier;
import fr.layer4.dek.http.NonProxyRoutePlanner;
import fr.layer4.dek.http.WithProxyAuthCredentialsProvider;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public HostnameVerifier hostnameVerifier() {
        return new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
    }

    @Bean
    public SchemePortResolver schemePortResolver() {
        return new DefaultSchemePortResolver();
    }

    @Bean
    public CloseableHttpClient httpClient(PropertyManager propertyManager, WithProxyAuthCredentialsProvider withProxyAuthCredentialsProvider, NonProxyRoutePlanner nonProxyRoutePlanner, ConfigurableHostnameVerifier configurableHostnameVerifier) {
        return HttpClientBuilder.create()
                .setSSLHostnameVerifier(configurableHostnameVerifier)
                .setRoutePlanner(nonProxyRoutePlanner)
                .setDefaultCredentialsProvider(withProxyAuthCredentialsProvider)
                // TODO
//                .setDefaultRequestConfig(
//                        RequestConfig.custom()
//                                .setSocketTimeout(Integer.valueOf(propertyManager.getProperty("http.socket.timeout").orElse("30000")))
//                                .setConnectTimeout(Integer.valueOf(propertyManager.getProperty("http.connect.timeout").orElse("30000")))
//                                .setProxyPreferredAuthSchemes(Arrays.asList("basic", "ntlm"))
//                                .build()
//                )
                .setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy())
                .build();
    }

    @Bean
    public RestTemplate restTemplate(CloseableHttpClient httpClient) {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}
