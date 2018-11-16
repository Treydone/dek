package fr.layer4.hhsl;

import fr.layer4.hhsl.store.LocalPropertyManager;
import fr.layer4.hhsl.store.LocalRegistryConnectionManager;
import fr.layer4.hhsl.store.LocalSecuredStore;
import org.junit.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.file.Paths;

import static fr.layer4.hhsl.store.LocalSecuredStore.getDatabasePath;

// TODO
public class MainIntegrationTest {

    @SuppressWarnings("squid:S2068")
    public static final String PASSWORD = "le_password";

    @Test
    public void info() {

        init();

        Main.main("info", "--unlock", PASSWORD);
        Main.main("list", "registry", "--unlock", PASSWORD);
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
