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

import fr.layer4.hhsl.prompt.Prompter;
import fr.layer4.hhsl.store.LocalSecuredStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class LocalClusterService implements ClusterService {

    public static final RowMapper<Cluster> CLUSTER_ROW_MAPPER = (r, i) -> {
        Cluster cluster = new Cluster();
        cluster.setId(r.getLong("id"));
        cluster.setName(r.getString("name"));
        cluster.setType(r.getString("type"));
        cluster.setUser(r.getString("user"));
        cluster.setPassword(r.getString("password"));
        cluster.setRegistry(Constants.LOCAL_REGISTRY_NAME);
        cluster.setUri(URI.create(r.getString("uri")));
        cluster.setBanner(r.getString("banner").getBytes());
        return cluster;
    };

    public static void updateDdl(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.batchUpdate(
                "CREATE TABLE IF NOT EXISTS local_cluster(id INT AUTO_INCREMENT PRIMARY KEY, type VARCHAR(255), name VARCHAR(255), uri VARCHAR(255), banner text, user VARCHAR(255), password VARCHAR(255))");
    }

    private final LocalSecuredStore localSecuredStore;
    private final Prompter prompter;

    @Override
    public Cluster addOrUpdateCluster(String type, String name, String uri, String banner) {

        String user = this.prompter.prompt("User:");
        String password = this.prompter.promptForPassword("Password:");

        this.localSecuredStore.getJdbcTemplate().update("MERGE INTO local_cluster KEY (`name`) VALUES (default, ?, ?, ?, ?, ?, ?);", type, name, uri, banner, user, password);

        return getCluster(name).orElseThrow(() -> new RuntimeException("Can get cluster"));
    }

    @Override
    public void deleteCluster(String name) {
        this.localSecuredStore.getJdbcTemplate().update("DELETE local_cluster WHERE `name` = ?", name);
    }

    @Override
    public List<Cluster> listClusters() {
        String query = "SELECT * FROM local_cluster";
        Object[] objects = {};
        return this.localSecuredStore.getJdbcTemplate().query(query, objects, CLUSTER_ROW_MAPPER);
    }

    @Override
    public Optional<Cluster> getCluster(String name) {
        try {
            return Optional.of(this.localSecuredStore.getJdbcTemplate().queryForObject("SELECT * FROM local_cluster WHERE `name` = ?", CLUSTER_ROW_MAPPER, name));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
