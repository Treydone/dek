package fr.layer4.dek;

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


import fr.layer4.dek.property.LocalPropertyManager;
import fr.layer4.dek.registry.LocalClusterService;
import fr.layer4.dek.registry.LocalRegistryConnectionManager;
import fr.layer4.dek.store.LocalSecuredStore;
import org.junit.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static fr.layer4.dek.store.LocalSecuredStore.getDatabasePath;

// TODO
public class MainIntegrationTest {

    @SuppressWarnings("squid:S2068")
    public static final String PASSWORD = "le_password";

    @Test
    public void info() throws URISyntaxException {

        init();

        Main.main("info", "--unlock", PASSWORD);

        Main.main("list", "registry", "--unlock", PASSWORD);
        Main.main("list", "cluster", "--unlock", PASSWORD);

        Main.main("list", "env", "--unlock", PASSWORD);
        Main.main("set", "env", "test-key", "test-value", "--unlock", PASSWORD);
        Main.main("set", "env", "test-key", "test-value2", "--unlock", PASSWORD);
        Main.main("get", "env", "test-key", "--unlock", PASSWORD);
        Main.main("list", "env", "--unlock", PASSWORD);

        Main.main("add", "cluster", "local", "hadoop-unit", "LOCAL-HU", MainIntegrationTest.class.getClassLoader().getResource("hadoop-unit").toURI().toString(), "--unlock", PASSWORD);
        Main.main("list", "cluster", "--unlock", PASSWORD);

    }

    // TODO Find a way to trap the reader in order to just do a Main.main("init");
    protected void init() {
        LocalSecuredStore localSecuredStore = new LocalSecuredStore(new ApplicationEventPublisher() {
            @Override
            public void publishEvent(ApplicationEvent applicationEvent) {

            }

            @Override
            public void publishEvent(Object o) {

            }
        });
        localSecuredStore.afterPropertiesSet();
        localSecuredStore.init(PASSWORD);
        LocalClusterService.updateDdl(localSecuredStore.getJdbcTemplate());
        LocalPropertyManager.updateDdl(localSecuredStore.getJdbcTemplate());
        LocalPropertyManager.updateData(localSecuredStore.getJdbcTemplate());
        LocalRegistryConnectionManager.updateDdl(localSecuredStore.getJdbcTemplate());
        LocalRegistryConnectionManager.updateData(localSecuredStore.getJdbcTemplate(), Paths.get(getDatabasePath()).toUri().toString());
        localSecuredStore.destroy();
    }
}
