package fr.layer4.dek.http;

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
    @SuppressWarnings("squid:S2068")
    String PROXY_AUTH_NTLM_PASSWORD = "proxy.auth.ntlm.password";
    String PROXY_AUTH_NTLM_DOMAIN = "proxy.auth.ntlm.domain";

    String PROXY_AUTH_BASIC_USER = "proxy.auth.basic.user";
    @SuppressWarnings("squid:S2068")
    String PROXY_AUTH_BASIC_PASSWORD = "proxy.auth.basic.password";
}
