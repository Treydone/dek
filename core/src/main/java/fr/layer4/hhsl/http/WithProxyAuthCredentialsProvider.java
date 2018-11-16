package fr.layer4.hhsl.http;

import fr.layer4.hhsl.PropertyManager;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WithProxyAuthCredentialsProvider extends BasicCredentialsProvider {

    private final PropertyManager propertyManager;

    @Autowired
    public WithProxyAuthCredentialsProvider(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
    }

    @Override
    public Credentials getCredentials(final AuthScope authscope) {
        // Check if proxy configuration is present
        if (Boolean.valueOf(this.propertyManager.getProperty(HttpProperties.PROXY_ENABLED).orElse(HttpProperties.PROXY_ENABLED_DEFAULT))) {

            // Check host and port are present
            String host = this.propertyManager.getProperty(HttpProperties.PROXY_HOST).orElseThrow(() -> new RuntimeException("Proxy is enabled but host is missing"));
            Integer port = Integer.valueOf(this.propertyManager.getProperty(HttpProperties.PROXY_PORT).orElseThrow(() -> new RuntimeException("Proxy is enabled but port is missing")));

            AuthScope newAuthscope = new AuthScope(host, port);
            if (authscope.equals(newAuthscope)) {

                // Check auth
                String proxyAuth = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_TYPE).orElse(HttpProperties.PROXY_AUTH_NONE);

                switch (proxyAuth.toLowerCase()) {
                    case HttpProperties.PROXY_AUTH_NONE:
                        break;
                    case HttpProperties.PROXY_AUTH_NTLM:
                        String ntlmUser = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_NTLM_USER).orElseThrow(() -> new RuntimeException("NTLM proxy authentication is enabled but user is missing"));
                        String ntlmPassword = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_NTLM_PASSWORD).orElseThrow(() -> new RuntimeException("NTLM proxy authentication is enabled but password is missing"));
                        String domain = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_NTLM_DOMAIN).orElseThrow(() -> new RuntimeException("NTLM proxy authentication is enabled but domain is missing"));
                        NTCredentials ntCredentials = new NTCredentials(ntlmUser, ntlmPassword, null, domain);
                        return ntCredentials;
                    case HttpProperties.PROXY_AUTH_BASIC:
                        String user = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_BASIC_USER).orElseThrow(() -> new RuntimeException("Basic proxy authentication is enabled but user is missing"));
                        String password = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_BASIC_PASSWORD).orElseThrow(() -> new RuntimeException("Basic proxy authentication is enabled but password is missing"));
                        UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(user, password);
                        return usernamePasswordCredentials;
                    default:
                        throw new RuntimeException("Unknown proxy auth " + proxyAuth);
                }
            }
        }
        return super.getCredentials(authscope);
    }
}


