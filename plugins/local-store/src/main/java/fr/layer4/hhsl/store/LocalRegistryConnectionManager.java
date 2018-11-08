package fr.layer4.hhsl.store;

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

import fr.layer4.hhsl.registry.RegistryConnection;
import fr.layer4.hhsl.registry.RegistryConnectionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalRegistryConnectionManager implements RegistryConnectionManager {

    private final LocalLockableStore localLockableStore;

    protected static void updateDdl(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.batchUpdate(
                "CREATE TABLE IF NOT EXISTS registry(id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), uri VARCHAR(255))");
    }

    protected static void updateData(JdbcTemplate jdbcTemplate, String databasePath) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO registry VALUES (default, 'local', 'local://" + databasePath + "')"
        );
    }

    public static final RowMapper<RegistryConnection> REGISTRY_ROW_MAPPER = (r, i) -> {
        RegistryConnection registryConnection = new RegistryConnection();
        registryConnection.setId(r.getLong("id"));
        registryConnection.setName(r.getString("name"));
        registryConnection.setUri(URI.create(r.getString("uri")));
        return registryConnection;
    };

    @Override
    public Optional<RegistryConnection> getRegistry(String name) {
        try {
            return Optional.of(this.localLockableStore.getJdbcTemplate().queryForObject("SELECT * FROM registry WHERE `name` = ?", REGISTRY_ROW_MAPPER, name));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteRegistry(String name) {
        if (!fr.layer4.hhsl.Constants.LOCAL_REGISTRY_NAME.equals(name)) {
            this.localLockableStore.getJdbcTemplate().update("DELETE registry WHERE `name` = ?", name);
        }
    }

    @Override
    public List<RegistryConnection> listRegistries() {
        return this.localLockableStore.getJdbcTemplate().query("SELECT * FROM registry", new Object[]{}, REGISTRY_ROW_MAPPER);
    }

    @Override
    public void addOrUpdateRegistry(String name, String uri) {
        if (!fr.layer4.hhsl.Constants.LOCAL_REGISTRY_NAME.equals(URI.create(uri).getScheme())) {
            this.localLockableStore.getJdbcTemplate().update("MERGE INTO registry KEY (`name`) VALUES (default, ?, ?);", name, uri);
        }
    }
}
