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
import fr.layer4.hhsl.store.LocalLockableStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.net.URI;
import java.util.List;

@Slf4j
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
        return cluster;
    };

    private final JdbcTemplate jdbcTemplate;
    private final Prompter prompter;

    public LocalClusterService(LocalLockableStore localLockableStore, Prompter prompter) {
        this.jdbcTemplate = localLockableStore.getJdbcTemplate();
        this.prompter = prompter;

        log.info("Create local_cluster table");
        this.jdbcTemplate.batchUpdate("CREATE TABLE IF NOT EXISTS local_cluster(id INT AUTO_INCREMENT PRIMARY KEY, type VARCHAR(255), name VARCHAR(255), uri VARCHAR(255), banner text, user VARCHAR(255), password VARCHAR(255))");
    }

    @Override
    public Cluster addCluster(String type, String name, String uri, String banner) {

        String user = this.prompter.prompt("User:");
        String password = this.prompter.promptForPassword("Password:");

        this.jdbcTemplate.update("MERGE INTO local_cluster KEY (`name`) VALUES (default, ?, ?, ?, ?, ?, ?);", type, name, uri, banner, user, password);

        return getCluster(name);
    }

    @Override
    public void deleteCluster(String name) {
        this.jdbcTemplate.update("DELETE local_cluster WHERE `name` = ?", name);
    }

    @Override
    public List<Cluster> listClusters() {
        String query = "SELECT * FROM local_cluster";
        Object[] objects = {};
        return this.jdbcTemplate.query(query, objects, CLUSTER_ROW_MAPPER);
    }

    @Override
    public Cluster getCluster(String name) {
        return this.jdbcTemplate.queryForObject("SELECT * FROM local_cluster WHERE `name` = ?", CLUSTER_ROW_MAPPER, name);
    }

}
