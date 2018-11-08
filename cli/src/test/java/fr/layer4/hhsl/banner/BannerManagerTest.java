package fr.layer4.hhsl.banner;

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
        assertThat(template).isEqualTo(Banner.DEFAULT_BANNER);
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
