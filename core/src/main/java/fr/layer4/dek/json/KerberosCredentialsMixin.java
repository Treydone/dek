package fr.layer4.dek.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("kerberos")
public abstract class KerberosCredentialsMixin {
    KerberosCredentialsMixin(@JsonProperty("principal") String username, @JsonProperty("password") String keytab) {
    }
}
