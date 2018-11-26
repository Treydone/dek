package fr.layer4.dek.http;

import fr.layer4.dek.PropertyManager;
import fr.layer4.dek.events.StoreReadyEvent;
import lombok.RequiredArgsConstructor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WithProxyAuthCredentialsProvider extends BasicCredentialsProvider implements ApplicationListener<StoreReadyEvent> {

    private final PropertyManager propertyManager;

    @Override
    public void onApplicationEvent(StoreReadyEvent storeReadyEvent) {
        init();
    }

    public void init() {
        // Check if proxy configuration is present
        if (Boolean.valueOf(this.propertyManager.getProperty(HttpProperties.PROXY_ENABLED).orElse(HttpProperties.PROXY_ENABLED_DEFAULT))) {

            // Check host and port are present
            String host = this.propertyManager.getProperty(HttpProperties.PROXY_HOST).orElseThrow(() -> new RuntimeException("Proxy is enabled but host is missing"));
            Integer port = Integer.valueOf(this.propertyManager.getProperty(HttpProperties.PROXY_PORT).orElseThrow(() -> new RuntimeException("Proxy is enabled but port is missing")));

            // Check auth
            String proxyAuth = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_TYPE).orElse(HttpProperties.PROXY_AUTH_NONE);
            AuthScope authscope = new AuthScope(host, port);

            switch (proxyAuth.toLowerCase()) {
                case HttpProperties.PROXY_AUTH_NONE:
                    break;
                case HttpProperties.PROXY_AUTH_NTLM:
                    String ntlmUser = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_NTLM_USER).orElseThrow(() -> new RuntimeException("NTLM proxy authentication is enabled but user is missing"));
                    String ntlmPassword = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_NTLM_PASSWORD).orElseThrow(() -> new RuntimeException("NTLM proxy authentication is enabled but password is missing"));
                    String domain = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_NTLM_DOMAIN).orElseThrow(() -> new RuntimeException("NTLM proxy authentication is enabled but domain is missing"));
                    NTCredentials ntCredentials = new NTCredentials(ntlmUser, ntlmPassword, null, domain);
                    super.setCredentials(authscope, ntCredentials);
                    break;
                case HttpProperties.PROXY_AUTH_BASIC:
                    String user = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_BASIC_USER).orElseThrow(() -> new RuntimeException("Basic proxy authentication is enabled but user is missing"));
                    String password = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_BASIC_PASSWORD).orElseThrow(() -> new RuntimeException("Basic proxy authentication is enabled but password is missing"));
                    UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(user, password);
                    super.setCredentials(authscope, usernamePasswordCredentials);
                    break;
                default:
                    throw new RuntimeException("Unknown proxy auth " + proxyAuth);
            }
        }
    }
}


