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

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class BannerManagerTest {

    private BannerManager bannerManager;

    @Before
    public void beforeEachTest() {
        this.bannerManager = new BannerManager();
    }

    @Test(expected = RuntimeException.class)
    public void unknownPath() {

        // Given
        String path = "unknown path";

        // When
        this.bannerManager.checkAndLoadTemplate(path);

        // Then
    }

    @Test
    public void emptyPath() {

        // Given
        String path = "";

        // When
        String template = this.bannerManager.checkAndLoadTemplate(path);

        // Then
        assertThat(template).isEqualTo(Banner.DEFAULT_CLUSTER_BANNER);
    }

    @Test(expected = RuntimeException.class)
    public void incorrectTemplate() throws IOException {

        // Given
        Path tempFile = Files.createTempFile("test", "template");
        Files.write(tempFile, "<#fail><#fail>".getBytes());
        String path = tempFile.toAbsolutePath().toString();

        // When
        this.bannerManager.checkAndLoadTemplate(path);

        // Then
    }

    @Test
    public void ok() throws IOException {

        // Given
        Path tempFile = Files.createTempFile("test", "template");
        String expectedTemplate = "<#list 1..width as x>-</#list>\nUsing this aweeeessommme ${cluster.name}\n<#list 1..width as x>-</#list>";
        Files.write(tempFile, expectedTemplate.getBytes());
        String path = tempFile.toAbsolutePath().toString();

        // When
        String template = this.bannerManager.checkAndLoadTemplate(path);

        // Then
        assertThat(template).isEqualTo(expectedTemplate);
    }
}
