package fr.layer4.hhsl.http;

import fr.layer4.hhsl.PropertyManager;
import lombok.RequiredArgsConstructor;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurableHostnameVerifier implements HostnameVerifier {

    private final PropertyManager propertyManager;
    private final HostnameVerifier defaultHostnameVerifier;

    @Override
    public boolean verify(String s, SSLSession sslSession) {
        if(Boolean.valueOf(propertyManager.getProperty("http.insecure").orElse("false"))) {
            return NoopHostnameVerifier.INSTANCE.verify(s, sslSession);
        } else {
            return this.defaultHostnameVerifier.verify(s, sslSession);
        }
    }
}
