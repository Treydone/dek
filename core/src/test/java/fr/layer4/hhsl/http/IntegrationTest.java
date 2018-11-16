package fr.layer4.hhsl.http;

import fr.layer4.hhsl.PropertyManager;
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
