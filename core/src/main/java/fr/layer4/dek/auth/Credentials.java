package fr.layer4.dek.auth;

/**
 * This interface represents a set of credentials consisting of a security
 * principal and a secret (password) that can be used to establish user
 * identity
 */
public interface Credentials {

    String getPrincipal();

    String getPassword();

    static UsernamePasswordCredentials basic(String username, String password) {
        return new UsernamePasswordCredentials(username, password);
    }
}