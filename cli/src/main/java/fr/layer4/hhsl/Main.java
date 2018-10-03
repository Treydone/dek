package fr.layer4.hhsl;

import lombok.extern.slf4j.Slf4j;
import org.jline.reader.MaskingCallback;
import org.jline.reader.impl.SimpleMaskingCallback;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.PromptProvider;

import java.util.Arrays;

@Slf4j
@SpringBootApplication
public class Main {

    public static void main(String... args) {
        SpringApplication app = new SpringApplication(Main.class);

        boolean noPromptMode = Arrays.stream(args)
                .filter(w -> !w.startsWith("@")).count() > 0;
        if (noPromptMode) {
            app.setBannerMode(Banner.Mode.OFF);
        }
        app.run(args);
    }

    @Bean
    public PromptProvider myPromptProvider() {
        return new UnlockablePromptProvider();
    }

    @Bean
    public ExitCodeExceptionMapper exitCodeExceptionMapper() {
        return exception -> {
            Throwable e = exception;
            while (e != null && !(e instanceof ExitRequest)) {
                e = e.getCause();
            }
            return e == null ? 1 : ((ExitRequest) e).status();
        };
    }

    @Bean
    public CommandLineRunner exampleCommandLineRunner(Shell shell, ConfigurableEnvironment environment) {
        return new NoInteractiveOnEmptyArgsCommandLineRunner(shell, environment);
    }

    @Bean
    public MaskingCallback maskingCallback() {
        return new SimpleMaskingCallback('*');
    }
}