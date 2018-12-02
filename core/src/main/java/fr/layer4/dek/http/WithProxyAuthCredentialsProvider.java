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


import fr.layer4.dek.DekException;
import fr.layer4.dek.property.PropertyManager;
import fr.layer4.dek.events.StoreReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
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
                    log.info("Set NT credentials {} for scope {}", ntCredentials, authscope);
                    super.setCredentials(authscope, ntCredentials);
                    break;
                case HttpProperties.PROXY_AUTH_BASIC:
                    String user = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_BASIC_USER).orElseThrow(() -> new RuntimeException("Basic proxy authentication is enabled but user is missing"));
                    String password = this.propertyManager.getProperty(HttpProperties.PROXY_AUTH_BASIC_PASSWORD).orElseThrow(() -> new RuntimeException("Basic proxy authentication is enabled but password is missing"));
                    UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(user, password);
                    log.info("Set basic credentials {} for scope {}", usernamePasswordCredentials, authscope);
                    super.setCredentials(authscope, usernamePasswordCredentials);
                    break;
                default:
                    throw new DekException("Unknown proxy auth " + proxyAuth);
            }
        }
    }
}


