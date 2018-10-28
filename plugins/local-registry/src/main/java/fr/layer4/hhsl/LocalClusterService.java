package fr.layer4.hhsl;

import fr.layer4.hhsl.prompt.Prompter;
import fr.layer4.hhsl.store.LockableLocalStore;
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

    private final LockableLocalStore lockableLocalStore;
    private final JdbcTemplate jdbcTemplate;
    private final Prompter prompter;

    public LocalClusterService(LockableLocalStore lockableLocalStore, Prompter prompter) {
        this.lockableLocalStore = lockableLocalStore;
        this.jdbcTemplate = lockableLocalStore.getJdbcTemplate();
        this.prompter = prompter;

        log.info("Create local_cluster table");
        this.jdbcTemplate.batchUpdate("CREATE TABLE local_cluster(id INT AUTO_INCREMENT PRIMARY KEY, type VARCHAR(255), name VARCHAR(255), uri VARCHAR(255), banner text, user VARCHAR(255), password VARCHAR(255))");
    }

    @Override
    public void addCluster(String type, String name, String uri, String banner) {

        String user = this.prompter.prompt("User:");
        String password = this.prompter.promptForPassword("Password:");

        this.jdbcTemplate.update("MERGE INTO local_cluster KEY (`name`) VALUES (default, ?, ?, ?, ?, ?, ?);", type, name, uri, banner, user, password);
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
        return this.jdbcTemplate.queryForObject("SELECT * FROM local_cluster WHERE `name` = ?", CLUSTER_ROW_MAPPER, new Object[]{name});
    }

}
