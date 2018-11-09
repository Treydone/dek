package fr.layer4.hhsl.banner;

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

import fr.layer4.hhsl.Cluster;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
public class BannerManager {

    public static final int DEFAULT_TERMINAL_WIDTH = 200;

    public String checkAndLoadTemplate(String path) {
        if (StringUtils.isNotBlank(path)) {
            File file = new File(path);
            if (!file.exists()) {
                throw new RuntimeException("Banner does not exist");
            } else {
                // Check template against fake data
                Cluster cluster = new Cluster();
                cluster.setType("test");
                cluster.setName("test");
                cluster.setUri(URI.create("file:///test"));
                cluster.setRegistry("local");
                cluster.setUser("le_user");
                cluster.setPassword("le_password");

                // Load template from file
                String template;
                try {
                    template = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException("Can not read banner file", e);
                }
                new Banner(template, cluster).render(DEFAULT_TERMINAL_WIDTH);

                return template;
            }
        } else {
            // Set to default banner
            return Banner.DEFAULT_BANNER;
        }
    }
}
