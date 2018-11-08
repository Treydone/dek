package fr.layer4.hhsl.banner;

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
