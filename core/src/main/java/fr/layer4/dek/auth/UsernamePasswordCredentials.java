package fr.layer4.dek.auth;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public class UsernamePasswordCredentials implements Credentials {

    private final String principal;
    private final String password;

    @Override
    public String getPrincipal() {
        return this.principal;
    }

    @Override
    public String getPassword() {
        return this.password;
    }
}
