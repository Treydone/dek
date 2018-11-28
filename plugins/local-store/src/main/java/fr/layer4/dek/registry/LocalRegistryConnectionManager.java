package fr.layer4.dek.registry;

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

import fr.layer4.dek.events.StoreReadyEvent;
import fr.layer4.dek.store.LocalSecuredStore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static fr.layer4.dek.store.LocalSecuredStore.getDatabasePath;

@Getter
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalRegistryConnectionManager implements RegistryConnectionManager, ApplicationListener<StoreReadyEvent> {

    private final LocalSecuredStore localSecuredStore;

    public static void updateDdl(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.batchUpdate(
                "CREATE TABLE IF NOT EXISTS registry(id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), uri VARCHAR(255))");
    }

    public static void updateData(JdbcTemplate jdbcTemplate, String databasePath) {
        jdbcTemplate.update("INSERT INTO registry VALUES (default, 'local', concat('local://', ?))", databasePath);
    }

    public static final RowMapper<RegistryConnection> REGISTRY_ROW_MAPPER = (r, i) -> {
        RegistryConnection registryConnection = new RegistryConnection();
        registryConnection.setId(r.getLong("id"));
        registryConnection.setName(r.getString("name"));
        registryConnection.setUri(URI.create(r.getString("uri")));
        return registryConnection;
    };

    @Override
    public void onApplicationEvent(StoreReadyEvent storeReadyEvent) {
        updateDdl(this.localSecuredStore.getJdbcTemplate());
        updateData(this.localSecuredStore.getJdbcTemplate(), Paths.get(getDatabasePath()).toUri().toString());
    }

    @Override
    public Optional<RegistryConnection> getRegistry(String name) {
        try {
            return Optional.of(this.localSecuredStore.getJdbcTemplate().queryForObject("SELECT * FROM registry WHERE `name` = ?", REGISTRY_ROW_MAPPER, name));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteRegistry(String name) {
        if (!fr.layer4.dek.Constants.LOCAL_REGISTRY_NAME.equals(name)) {
            this.localSecuredStore.getJdbcTemplate().update("DELETE registry WHERE `name` = ?", name);
        }
    }

    @Override
    public List<RegistryConnection> listRegistries() {
        return this.localSecuredStore.getJdbcTemplate().query("SELECT * FROM registry", new Object[]{}, REGISTRY_ROW_MAPPER);
    }

    @Override
    public void addOrUpdateRegistry(String name, String uri) {
        if (!fr.layer4.dek.Constants.LOCAL_REGISTRY_NAME.equals(URI.create(uri).getScheme())) {
            this.localSecuredStore.getJdbcTemplate().update("MERGE INTO registry KEY (`name`) VALUES (default, ?, ?);", name, uri);
        }
    }
}
