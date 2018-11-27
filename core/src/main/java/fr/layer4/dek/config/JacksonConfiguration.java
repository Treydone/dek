package fr.layer4.dek.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.layer4.dek.auth.Credentials;
import fr.layer4.dek.auth.KerberosCredentials;
import fr.layer4.dek.auth.UsernamePasswordCredentials;
import fr.layer4.dek.json.CredentialsMixin;
import fr.layer4.dek.json.KerberosCredentialsMixin;
import fr.layer4.dek.json.UsernamePasswordCredentialsMixin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class JacksonConfiguration {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        configure(this.objectMapper);
    }

    public static void configure(ObjectMapper objectMapper) {
        objectMapper.addMixIn(Credentials.class, CredentialsMixin.class);
        objectMapper.addMixIn(UsernamePasswordCredentials.class, UsernamePasswordCredentialsMixin.class);
        objectMapper.addMixIn(KerberosCredentials.class, KerberosCredentialsMixin.class);
        objectMapper.registerSubtypes(UsernamePasswordCredentials.class, KerberosCredentials.class);
    }
}
