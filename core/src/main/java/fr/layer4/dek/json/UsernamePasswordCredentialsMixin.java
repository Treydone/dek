package fr.layer4.dek.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("basic")
public abstract class UsernamePasswordCredentialsMixin {
    UsernamePasswordCredentialsMixin(@JsonProperty("principal") String principal, @JsonProperty("password") String password) {
    }
}
