package fr.layer4.hhsl.local;

import lombok.extern.slf4j.Slf4j;
import org.h2.engine.Constants;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.store.FileLister;
import org.h2.store.FileStore;
import org.h2.tools.DeleteDbFiles;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class MyTest {

    @Test
    public void test() throws SQLException, IOException {

        DeleteDbFiles.execute(H2LocalStore.getRootPath(), H2LocalStore.DB, true);
        JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.create(H2LocalStore.JDBC_H2 + H2LocalStore.getDatabasePath(), H2LocalStore.USER, H2LocalStore.PASSWORD);

        try (Connection connection = jdbcConnectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("create table test(id int primary key, name varchar(255))")) {
            preparedStatement.execute();
        }

        jdbcConnectionPool.dispose();

        log.debug("Checking if database exists in {}...", H2LocalStore.getRootPath());
        List<String> files = FileLister.getDatabaseFiles(H2LocalStore.getRootPath(), H2LocalStore.DB, true);
        try {
            FileLister.tryUnlockDatabase(files, "encryption");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        files = FileLister.getDatabaseFiles(H2LocalStore.getRootPath(), H2LocalStore.DB, false);
        if (files.size() == 0) {
            // Not ready
            log.warn("Database is not initialised");
            return;
        }

        // Ready!
        log.debug("Database is ready");

        log.debug("Checking if database is encrypted...");

        String header = "H2encrypt\n";
        byte[] HEADER = header.getBytes();

        FileChannel fc = new FileInputStream(files.stream().filter(f -> f.endsWith(Constants.SUFFIX_MV_FILE)).findFirst().get()).getChannel();
        ByteBuffer buff = ByteBuffer.allocate(HEADER.length);
        fc.read(buff);
        buff.flip();
        String res = new String(buff.array());

        System.err.println(res);

        jdbcConnectionPool = JdbcConnectionPool.create(H2LocalStore.JDBC_H2 + H2LocalStore.getDatabasePath(), H2LocalStore.USER, H2LocalStore.PASSWORD);
    }
}
