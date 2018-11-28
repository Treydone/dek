package fr.layer4.dek.banner;

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

import fr.layer4.dek.DekException;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import lombok.Data;
import org.springframework.shell.TerminalSizeAware;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Data
public class Banner implements TerminalSizeAware {

    public static final String DEFAULT_CLUSTER_BANNER = "<#list 1..width as x>-</#list>\nUsing ${cluster.name}\n<#list 1..width as x>-</#list>";
    private final String template;
    private final Map<String, Object> model;

    @Override
    public CharSequence render(int terminalWidth) {
        Version version = Configuration.VERSION_2_3_28;

        Configuration configuration = new Configuration(version);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);

        BeansWrapper wrapper = new BeansWrapper(version);
        TemplateModel statics = wrapper.getStaticModels();

        Map<String, Object> model = new HashMap<>();
        model.putAll(this.model);
        model.put("width", terminalWidth);
        model.put("statics", statics);

        Template temp;
        try {
            temp = new Template("name", new StringReader(this.template), configuration);
        } catch (IOException e) {
            throw new DekException(e);
        }

        StringWriter out = new StringWriter();
        try {
            temp.process(model, out);
        } catch (Exception e) {
            throw new DekException(e);
        }

        return out.toString();
    }
}
