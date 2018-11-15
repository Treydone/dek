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

import fr.layer4.hhsl.events.StoreReadyEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.h2.engine.Constants;
import org.h2.jdbc.JdbcSQLException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.store.FileLister;
import org.h2.tools.ChangeFileEncryption;
import org.h2.tools.DeleteDbFiles;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * http://www.h2database.com/html/features.html#file_encryption
 */
@Getter
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalSecuredStore implements SecuredStore, InitializingBean, DisposableBean {

    public static final String CIPHER = "AES";
    public static final String DB = "db";
    public static final String USER = "sa";
    @SuppressWarnings("squid:S2068")
    public static final String PASSWORD = "sa";
    public static final String JDBC_H2 = "jdbc:h2:";
    public static final String CIPHER_EXTENSION = ";CIPHER=";
    public static final String ENCRYPT_HEADER = "H2encrypt\n";

    private final ApplicationEventPublisher applicationEventPublisher;
    private JdbcConnectionPool dataSource;
    private JdbcTemplate jdbcTemplate;

    private boolean isReady = false;

    public void purge() {
        if (this.dataSource != null) {
            log.debug("Dispose database");
            this.dataSource.dispose();
        }
        log.debug("Clean local file");
        DeleteDbFiles.execute(fr.layer4.hhsl.Constants.getRootPath(), DB, true);
        this.isReady = false;
    }

    @Override
    public void afterPropertiesSet() {
        log.debug("Checking if database exists in {}...", fr.layer4.hhsl.Constants.getRootPath());
        List<String> files = getDatabaseFiles();
        if (files.size() == 0) {
            // Not ready
            log.warn("Database is not initialised");
            return;
        }

        // Ready!
        log.debug("Database is ready");
        this.isReady = true;

        log.debug("Checking if database is encrypted...");
        boolean encrypted = isEncrypted(files);
        if (!encrypted) {
            // Lock!
            throw new RuntimeException("Local database doesn't seems to be encrypted");
        }
    }

    @Override
    public void unlock(String password) {
        password += " " + PASSWORD;

        this.dataSource = JdbcConnectionPool.create(JDBC_H2 + getDatabasePath() + CIPHER_EXTENSION + CIPHER, USER, password);
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        checkUnlockWithPassword();
    }

    protected void checkUnlockWithPassword() {
        try {
            this.jdbcTemplate.execute("select 1");
        } catch (DataAccessException e) {
            if (e.getCause() instanceof JdbcSQLException) {
                JdbcSQLException jdbcSQLException = (JdbcSQLException) e.getCause();
                if (jdbcSQLException.getErrorCode() == 90049) {
                    throw new RuntimeException("Wrong password");
                }
            }
            throw e;
        }
    }

    @Override
    public void destroy() {
        if (this.dataSource != null) {
            this.dataSource.dispose();
        }
    }

    @Override
    public boolean isReady() {
        return this.isReady;
    }

    @Override
    public void init(String password) {
        // Prepare location of database
        purge();

        // Ask for root password
        this.dataSource = JdbcConnectionPool.create(JDBC_H2 + getDatabasePath() + CIPHER_EXTENSION + CIPHER, USER, password + " " + PASSWORD);
        this.isReady = true;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.applicationEventPublisher.publishEvent(new StoreReadyEvent(this.jdbcTemplate));
    }

    @Override
    public void changePassword(String actualPassword, String newPassword) {
        this.isReady = false;

        // Test connection with provided password
        checkUnlockWithPassword();

        // Dispose current datasource
        this.dataSource.dispose();

        // Change encryption password
        try {
            ChangeFileEncryption.execute(fr.layer4.hhsl.Constants.getRootPath(), DB, CIPHER_EXTENSION + CIPHER, actualPassword.toCharArray(), newPassword.toCharArray(), true);
        } catch (SQLException e) {
            throw new RuntimeException("Can not change password", e);
        }

        // Create new datasource and jdbctemplate
        this.dataSource = JdbcConnectionPool.create(JDBC_H2 + getDatabasePath() + CIPHER_EXTENSION + CIPHER, USER, newPassword + " " + PASSWORD);
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);

        this.isReady = true;
    }

    protected static String getDatabasePath() {
        return fr.layer4.hhsl.Constants.getRootPath() + File.separator + DB;
    }

    protected List<String> getDatabaseFiles() {
        List<String> files = FileLister.getDatabaseFiles(fr.layer4.hhsl.Constants.getRootPath(), DB, true);
        try {
            FileLister.tryUnlockDatabase(files, "encryption");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        files = FileLister.getDatabaseFiles(fr.layer4.hhsl.Constants.getRootPath(), DB, false);
        return files;
    }

    protected boolean isEncrypted(List<String> files) {
        try {
            Optional<String> file = files.stream().filter(f -> f.endsWith(Constants.SUFFIX_MV_FILE)).findFirst();
            if (!file.isPresent()) {
                return false;
            }

            String name = file.get();
            try (FileChannel fileChannel = new FileInputStream(name).getChannel()) {
                ByteBuffer buffer = ByteBuffer.allocate(ENCRYPT_HEADER.getBytes().length);
                fileChannel.read(buffer);
                buffer.flip();
                String res = new String(buffer.array());

                if (ENCRYPT_HEADER.equals(res)) {
                    // Lock!
                    log.debug("Database is encrypted");
                    return true;
                }
                log.debug("Database is unprotected");
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
