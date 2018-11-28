
package io.specto.hoverfly.junit.dsl;

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


import io.specto.hoverfly.junit.core.model.Response;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ResponseBuilder {
    private final Map<String, List<String>> headers = new HashMap();
    private String body = "";
    private int status = 200;
    private boolean templated = true;
    private final Map<String, String> transitionsState = new HashMap();
    private final List<String> removesState = new ArrayList();
    private int delay;
    private TimeUnit delayTimeUnit;

    // ---Custom
    private boolean encoded;

    public ResponseBuilder encoded(final boolean encoded) {
        this.encoded = encoded;
        return this;
    }
    // ---Custom

    ResponseBuilder() {
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static ResponseBuilder response() {
        return new ResponseBuilder();
    }

    public ResponseBuilder body(String body) {
        this.body = body;
        return this;
    }

    public ResponseBuilder status(int status) {
        this.status = status;
        return this;
    }

    public ResponseBuilder header(String key, String value) {
        this.headers.put(key, Collections.singletonList(value));
        return this;
    }

    public ResponseBuilder andSetState(String key, String value) {
        this.transitionsState.put(key, value);
        return this;
    }

    public ResponseBuilder andRemoveState(String stateToRemove) {
        this.removesState.add(stateToRemove);
        return this;
    }

    Response build() {
        return new Response(this.status, this.body, encoded, this.templated, this.headers, this.transitionsState, this.removesState);
    }

    public ResponseBuilder body(HttpBodyConverter httpBodyConverter) {
        this.body = httpBodyConverter.body();
        this.header("Content-Type", httpBodyConverter.contentType());
        return this;
    }

    public ResponseBuilder disableTemplating() {
        this.templated = false;
        return this;
    }

    public ResponseBuilder withDelay(int delay, TimeUnit delayTimeUnit) {
        this.delay = delay;
        this.delayTimeUnit = delayTimeUnit;
        return this;
    }

    ResponseDelaySettingsBuilder addDelay() {
        return new ResponseDelaySettingsBuilder(this.delay, this.delayTimeUnit);
    }
}
