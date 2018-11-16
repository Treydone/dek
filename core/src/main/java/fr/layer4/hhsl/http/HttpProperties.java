package fr.layer4.hhsl.http;

public interface HttpProperties {
    String PROXY_ENABLED = "proxy.enabled";
    String PROXY_ENABLED_DEFAULT = "false";

    String PROXY_HOST = "proxy.host";
    String PROXY_PORT = "proxy.port";

    String PROXY_NON_PROXY_HOSTS = "proxy.non-proxy-hosts";
    String PROXY_NON_PROXY_HOSTS_DEFAULT = "127.0.0.1, localhost";

    String PROXY_AUTH_TYPE = "proxy.auth.type";
    String PROXY_AUTH_NONE = "none";
    String PROXY_AUTH_NTLM = "ntlm";
    String PROXY_AUTH_BASIC = "basic";

    String PROXY_AUTH_NTLM_USER = "proxy.auth.ntlm.user";
    String PROXY_AUTH_NTLM_PASSWORD = "proxy.auth.ntlm.password";
    String PROXY_AUTH_NTLM_DOMAIN = "proxy.auth.ntlm.domain";

    String PROXY_AUTH_BASIC_USER = "proxy.auth.basic.user";
    String PROXY_AUTH_BASIC_PASSWORD = "proxy.auth.basic.password";
}
