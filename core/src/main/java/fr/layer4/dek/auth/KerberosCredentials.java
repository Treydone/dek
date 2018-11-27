package fr.layer4.dek.auth;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public class KerberosCredentials implements Credentials {

    private final String username;
    private final String keytab;

    @Override
    public String getPrincipal() {
        return username;
    }

    @Override
    public String getPassword() {
        return keytab;
    }
}
