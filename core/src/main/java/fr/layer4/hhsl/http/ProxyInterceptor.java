package fr.layer4.hhsl.http;

import fr.layer4.hhsl.PropertyManager;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProxyInterceptor implements HttpRequestInterceptor {

    private final PropertyManager propertyManager;

    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) {
        if (httpRequest instanceof HttpRequestBase) {

            RequestConfig.Builder builder = RequestConfig.custom()
                    .setSocketTimeout(Integer.valueOf(propertyManager.getProperty("http.socket.timeout").orElse("30000")))
                    .setConnectTimeout(Integer.valueOf(propertyManager.getProperty("http.connect.timeout").orElse("30000")));

            // Check if proxy configuration is present
            if (Boolean.valueOf(propertyManager.getProperty("proxy.enabled").orElse("false"))) {

                // Check host and port are present
                String host = propertyManager.getProperty("proxy.host").orElseThrow(() -> new RuntimeException("Proxy is enabled but host is missing"));
                Integer port = Integer.valueOf(propertyManager.getProperty("proxy.port").orElseThrow(() -> new RuntimeException("Proxy is enabled but port is missing")));
                HttpHost proxy = new HttpHost(host, port);
                builder = builder.setProxy(proxy);

                // Check auth
                String proxyAuth = propertyManager.getProperty("proxy.auth.type").orElse("none");
                switch (proxyAuth.toLowerCase()) {
                    case "none":
                        break;
                    case "ntlm":
                        String ntlmUser = propertyManager.getProperty("proxy.auth.ntlm.user").orElseThrow(() -> new RuntimeException("NTLM proxy authentication is enabled but user is missing"));
                        String ntlmPassword = propertyManager.getProperty("proxy.auth.ntlm.password").orElseThrow(() -> new RuntimeException("NTLM proxy authentication is enabled but password is missing"));
                        String domain = propertyManager.getProperty("proxy.auth.ntlm.domain").orElseThrow(() -> new RuntimeException("NTLM proxy authentication is enabled but domain is missing"));
                        CredentialsProvider ntlmCredentialsProvider = new BasicCredentialsProvider();
                        ntlmCredentialsProvider.setCredentials(new AuthScope(host, port), new NTCredentials(ntlmUser, ntlmPassword, null, domain));
                        httpContext.setAttribute(HttpClientContext.CREDS_PROVIDER, ntlmCredentialsProvider);
                        builder = builder.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM));
                        break;
                    case "basic":
                        String user = propertyManager.getProperty("proxy.auth.basic.user").orElseThrow(() -> new RuntimeException("Basic proxy authentication is enabled but user is missing"));
                        String password = propertyManager.getProperty("proxy.auth.basic.password").orElseThrow(() -> new RuntimeException("Basic proxy authentication is enabled but password is missing"));
                        CredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
                        basicCredentialsProvider.setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials(user, password));
                        builder = builder.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC));
                        break;
                    default:
                        throw new RuntimeException("Unknown proxy auth " + proxyAuth);
                }
            }
            ((HttpRequestBase) httpRequest).setConfig(builder.build());
        }
    }
}