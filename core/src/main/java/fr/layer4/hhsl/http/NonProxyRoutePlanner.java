package fr.layer4.hhsl.http;

import fr.layer4.hhsl.PropertyManager;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component
public class NonProxyRoutePlanner extends DefaultRoutePlanner {

    private final PropertyManager propertyManager;

    @Autowired
    public NonProxyRoutePlanner(SchemePortResolver schemePortResolver, PropertyManager propertyManager) {
        super(schemePortResolver);
        this.propertyManager = propertyManager;
    }

    @Override
    protected HttpHost determineProxy(HttpHost target, HttpRequest request, HttpContext context) {
        if (Boolean.valueOf(propertyManager.getProperty(HttpProperties.PROXY_ENABLED).orElse(HttpProperties.PROXY_ENABLED_DEFAULT))) {
            String nonProxyHosts = propertyManager.getProperty(HttpProperties.PROXY_NON_PROXY_HOSTS).orElse(HttpProperties.PROXY_NON_PROXY_HOSTS_DEFAULT);

            if (Arrays.stream(nonProxyHosts.split(",")).map(String::trim).noneMatch(ex -> target.getHostName().contains(ex))) {

                String host = propertyManager.getProperty(HttpProperties.PROXY_HOST).orElseThrow(() -> new RuntimeException("Proxy is enabled but host is missing"));
                Integer port = Integer.valueOf(propertyManager.getProperty(HttpProperties.PROXY_PORT).orElseThrow(() -> new RuntimeException("Proxy is enabled but port is missing")));

                HttpHost proxy = new HttpHost(host, port);
                return proxy;
            }
        }
        return null;
    }
}

