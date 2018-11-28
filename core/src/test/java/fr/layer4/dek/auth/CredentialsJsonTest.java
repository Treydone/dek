package fr.layer4.dek.auth;

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
