package fr.layer4.hhsl.http;

import fr.layer4.hhsl.PropertyManager;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
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
public class WithProxyAuthCredentialsProviderTest {

    @Mock
    private PropertyManager propertyManager;

    private WithProxyAuthCredentialsProvider withProxyAuthCredentialsProvider;

    @Before
    public void beforeEachTest() {
        this.withProxyAuthCredentialsProvider = new WithProxyAuthCredentialsProvider(this.propertyManager);
        this.withProxyAuthCredentialsProvider.init();
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
        AuthScope authScope = new AuthScope("hhsl.fr", 80);
        

        // When
        Credentials credentials = this.withProxyAuthCredentialsProvider.getCredentials(authScope);

        // Then
        assertThat(credentials).isNull();
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_ENABLED);
    }

    @Test
    public void proxyWithoutAuth() {

        // Given
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_ENABLED)).thenReturn(Optional.of("true"));
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_HOST)).thenReturn(Optional.of("leproxy.fr"));
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_PORT)).thenReturn(Optional.of("546"));
        AuthScope authScope = new AuthScope("leproxy.fr", 546);
        

        // When
        Credentials credentials = this.withProxyAuthCredentialsProvider.getCredentials(authScope);

        // Then
        assertThat(credentials).isNull();
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_ENABLED);
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_HOST);
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_PORT);
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_AUTH_TYPE);
    }

    @Test
    public void proxyWithBasicAuth() {

        // Given
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_ENABLED)).thenReturn(Optional.of("true"));
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_HOST)).thenReturn(Optional.of("leproxy.fr"));
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_PORT)).thenReturn(Optional.of("546"));
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_TYPE)).thenReturn(Optional.of(HttpProperties.PROXY_AUTH_BASIC));
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_BASIC_USER)).thenReturn(Optional.of("le_user"));
        Mockito.when(this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_BASIC_PASSWORD)).thenReturn(Optional.of("le_password"));
        AuthScope authScope = new AuthScope("leproxy.fr", 546);
        

        // When
        Credentials credentials = this.withProxyAuthCredentialsProvider.getCredentials(authScope);

        // Then
        assertThat(credentials).isNotNull().isEqualTo(new UsernamePasswordCredentials("le_user", "le_password"));
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_ENABLED);
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_HOST);
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_PORT);
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_AUTH_TYPE);
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_AUTH_BASIC_USER);
        Mockito.verify(this.propertyManager).getProperty(HttpProperties.PROXY_AUTH_BASIC_PASSWORD);
    }
}