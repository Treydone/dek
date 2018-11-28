package fr.layer4.dek.config;

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
