package fr.layer4.hhsl.binaries;

/*-
 * #%L
 * HHSL
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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Find the closest mirror for an Apache project.
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApacheMirrorFinder {

    private final RestTemplate restTemplate;

    public String resolve(String binary) {
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("binary", binary);
        URI uri = UriComponentsBuilder
                .fromUriString("http://www.apache.org/dyn/closer.cgi/{binary}?as_json=1")
                .buildAndExpand(uriVariables).toUri();

        ResponseEntity<DynCloser> dynCloserResponseEntity = this.restTemplate.getForEntity(uri, DynCloser.class);
        return dynCloserResponseEntity.getBody().getPreferredServer() + dynCloserResponseEntity.getBody().getPathInfo();

    }

    @Data
    public static class DynCloser {
        @JsonProperty("backup")
        private List<String> backupServers;
        @JsonProperty("ftp")
        private List<String> ftpServers;
        @JsonProperty("http")
        private List<String> httpServers;
        @JsonProperty("in_attic")
        private boolean inAttic;
        @JsonProperty("in_dist")
        private boolean inDist;
        @JsonProperty("cca2")
        private String countryCode;
        @JsonProperty("path_info")
        private String pathInfo;
        @JsonProperty("preferred")
        private String preferredServer;
    }
}
