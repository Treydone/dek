package fr.layer4.dek.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.layer4.dek.config.JacksonConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CredentialsJsonTest {

    private ObjectMapper objectMapper;

    @Before
    public void beforeEachTest() {
        this.objectMapper = new ObjectMapper();
        JacksonConfiguration.configure(this.objectMapper);
    }

    @Test
    public void kerberos() throws Exception {

        // Given
        Credentials credentials = new KerberosCredentials("user@DOMAIN", "keytab");

        // When
        String json = this.objectMapper.writeValueAsString(credentials);

        // Then
        assertThat(json).isEqualTo("{\"type\":\"kerberos\",\"principal\":\"user@DOMAIN\",\"password\":\"keytab\"}");
        assertThat(this.objectMapper.readValue(json, Credentials.class)).isEqualTo(credentials);

    }

    @Test
    public void basic() throws Exception {

        // Given
        Credentials credentials = new UsernamePasswordCredentials("user", "password");

        // When
        String json = this.objectMapper.writeValueAsString(credentials);

        // Then
        assertThat(json).isEqualTo("{\"type\":\"basic\",\"principal\":\"user\",\"password\":\"password\"}");
        assertThat(this.objectMapper.readValue(json, Credentials.class)).isEqualTo(credentials);

    }
}
