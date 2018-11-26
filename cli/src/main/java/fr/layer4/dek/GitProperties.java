package fr.layer4.dek;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("git")
public class GitProperties {
    private String version;
    private String tags;
    private String branch;
    private String dirty;
    private String remote;
    private Commit commit;
    private Build build;

    @Data
    public static class Commit {
        private String id;
        private User user;
        private String time;
        private Message message;
    }

    @Data
    public static class Message {
        private String full;
    }

    @Data
    public static class Build {
        private User user;
        private String time;
    }

    @Data
    public static class User {
        private String name;
        private String email;
    }
}
