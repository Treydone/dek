package fr.layer4.hhsl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Order(InteractiveShellApplicationRunner.PRECEDENCE - 2)
class NoInteractiveOnEmptyArgsCommandLineRunner implements CommandLineRunner {

    private Shell shell;

    private final ConfigurableEnvironment environment;

    public NoInteractiveOnEmptyArgsCommandLineRunner(Shell shell, ConfigurableEnvironment environment) {
        this.shell = shell;
        this.environment = environment;
    }

    @Override
    public void run(String... args) throws Exception {
        List<String> commandsToRun = Arrays.stream(args)
                .filter(w -> !w.startsWith("@"))
                .collect(Collectors.toList());

        int i = commandsToRun.indexOf("--no-prompt");
        if (i > -1) {
            if (i + 1 > commandsToRun.size()) {
                log.error("Missing password");
                System.exit(1);
            }
        }

        if (!commandsToRun.isEmpty()) {
            InteractiveShellApplicationRunner.disable(environment);
            shell.run(new StringInputProvider(commandsToRun));
        }
    }
}
