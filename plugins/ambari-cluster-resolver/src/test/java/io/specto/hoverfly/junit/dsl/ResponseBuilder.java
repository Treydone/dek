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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;

public class ResponseBuilder {


    private final Map<String, List<String>> headers = new HashMap<>();
    private String body = "";
    private int status = 200;
    private boolean encoded = false;
    private boolean templated = true;

    private int delay;
    private TimeUnit delayTimeUnit;

    ResponseBuilder() {
    }

    /**
     * Instantiates a new instance
     *
     * @return the builder
     */
    @Deprecated
    public static ResponseBuilder response() {
        return new ResponseBuilder();
    }

    /**
     * Sets the body
     *
     * @param body body of the response
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder body(final String body) {
        this.body = body;
        return this;
    }

    /**
     * Sets the status
     *
     * @param status status of the response
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder status(final int status) {
        this.status = status;
        return this;
    }

    public ResponseBuilder encoded(final boolean encoded) {
        this.encoded = encoded;
        return this;
    }

    /**
     * Sets a header
     *
     * @param key   header name
     * @param value header value
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder header(final String key, final String value) {
        this.headers.put(key, singletonList(value));
        return this;
    }

    /**
     * Builds a {@link Response}
     *
     * @return the response
     */
    Response build() {
        return new Response(status, body, encoded, templated, headers);
    }

    public ResponseBuilder body(final HttpBodyConverter httpBodyConverter) {
        this.body = httpBodyConverter.body();
        this.header("Content-Type", httpBodyConverter.contentType());
        return this;
    }


    public ResponseBuilder disableTemplating() {
        this.templated = false;
        return this;
    }

    /**
     * Sets delay paramters.
     *
     * @param delay         amount of delay
     * @param delayTimeUnit time unit of delay (e.g. SECONDS)
     * @return the {@link ResponseBuilder for further customizations}
     */
    public ResponseBuilder withDelay(int delay, TimeUnit delayTimeUnit) {
        this.delay = delay;
        this.delayTimeUnit = delayTimeUnit;
        return this;
    }

    ResponseDelaySettingsBuilder addDelay() {
        return new ResponseDelaySettingsBuilder(delay, delayTimeUnit);
    }

}
