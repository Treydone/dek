package fr.layer4.hhsl;

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
