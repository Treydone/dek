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

import fr.layer4.hhsl.event.LockedEvent;
import fr.layer4.hhsl.event.StoreDestroyEvent;
import fr.layer4.hhsl.event.StoreReadyEvent;
import fr.layer4.hhsl.event.UnlockedEvent;
import fr.layer4.hhsl.prompt.Prompter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.h2.engine.Constants;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.store.FileLister;
import org.h2.tools.ChangeFileEncryption;
import org.h2.tools.DeleteDbFiles;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * http://www.h2database.com/html/features.html#file_encryption
 */
@Slf4j
@Component
public class LocalLockableStore implements LockableStore, InitializingBean, DisposableBean {

    public static final String CIPHER = "AES";
    public static final String DB = "db";
    public static final String USER = "sa";
    public static final String PASSWORD = "sa";
    public static final String JDBC_H2 = "jdbc:h2:";
    public static final String CIPHER_EXTENSION = ";CIPHER=";
    public static final String ENCRYPT_HEADER = "H2encrypt\n";

    @Setter
    @Autowired
    private Prompter prompter;

    @Setter
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Getter
    private JdbcConnectionPool dataSource;
    @Getter
    private JdbcTemplate jdbcTemplate;

    private boolean isUnlocked = false;
    private boolean isReady = false;

    public void purge() {
        if (dataSource != null) {
            log.debug("Dispose database");
            dataSource.dispose();
        }
        log.debug("Clean local file");
        DeleteDbFiles.execute(fr.layer4.hhsl.Constants.getRootPath(), DB, true);
        isReady = false;
        applicationEventPublisher.publishEvent(new StoreDestroyEvent(""));
        isUnlocked = false;
        applicationEventPublisher.publishEvent(new LockedEvent(""));
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
        isReady = true;
        applicationEventPublisher.publishEvent(new StoreReadyEvent(""));

        log.debug("Checking if database is encrypted...");

        boolean encrypted = isEncrypted(files);

        if (encrypted) {
            // Lock!
            isUnlocked = false;
            applicationEventPublisher.publishEvent(new LockedEvent(""));
            return;
        }
        log.debug("Database is unprotected");
        isUnlocked = true;
        applicationEventPublisher.publishEvent(new UnlockedEvent(""));

        dataSource = JdbcConnectionPool.create(JDBC_H2 + getDatabasePath(), USER, PASSWORD);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void unlock() {
        String password = prompter.promptForRootPassword();

        List<String> databaseFiles = getDatabaseFiles();
        boolean encrypted = isEncrypted(databaseFiles);

        if (encrypted) {
            password += " " + PASSWORD;
        }

        dataSource = JdbcConnectionPool.create(JDBC_H2 + getDatabasePath() + CIPHER_EXTENSION + CIPHER, USER, password);
        jdbcTemplate = new JdbcTemplate(dataSource);
        isUnlocked = true;
        applicationEventPublisher.publishEvent(new UnlockedEvent(""));
    }

    @Override
    public void destroy() {
        if (dataSource != null) {
            dataSource.dispose();
        }
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public boolean isUnlocked() {
        return isUnlocked;
    }

    @Override
    public void init() {
        init(false);
    }

    @Override
    public void init(boolean secured) {
        // Prepare location of database
        purge();

        String extension = "";
        String password = PASSWORD;
        // Ask for root password if needed
        if (secured) {
            password = prompter.doublePromptForPassword() + " " + PASSWORD;
            extension += CIPHER_EXTENSION + CIPHER;
        }

        log.info("Create database (secured:{})", secured);
        dataSource = JdbcConnectionPool.create(JDBC_H2 + getDatabasePath() + extension, USER, password);

        isReady = true;
        applicationEventPublisher.publishEvent(new StoreReadyEvent(""));
        isUnlocked = true;
        applicationEventPublisher.publishEvent(new UnlockedEvent(""));

        jdbcTemplate = new JdbcTemplate(dataSource);
        log.debug("Create tables");
        LocalPropertyManager.updateDdl(jdbcTemplate);
        LocalRegistryConnectionManager.updateDdl(jdbcTemplate);
        log.debug("Create default local registry");
        LocalRegistryConnectionManager.updateData(jdbcTemplate, getDatabasePath());
        log.debug("Create default properties");
        LocalPropertyManager.updateData(jdbcTemplate);
    }

    @Override
    public void changePassword() {
        isUnlocked = false;
        applicationEventPublisher.publishEvent(new LockedEvent(""));
        isReady = false;
        applicationEventPublisher.publishEvent(new StoreDestroyEvent(""));

        // Check if encrypted or not
        List<String> databaseFiles = getDatabaseFiles();
        boolean encrypted = isEncrypted(databaseFiles);

        if (!encrypted) {
            log.warn("Database is not encrypted");
            return;
        }

        String actualPassword = prompter.promptForRootPassword();

        // Test connection with provided password
        try {
            DriverManager.getConnection(JDBC_H2 + getDatabasePath() + CIPHER_EXTENSION + CIPHER, USER, actualPassword);
        } catch (SQLException e) {
            log.error("Oups", e);
            throw new RuntimeException("");
        }

        String newPassword = prompter.doublePromptForPassword();

        try {
            ChangeFileEncryption.execute(fr.layer4.hhsl.Constants.getRootPath(), DB, CIPHER, actualPassword.toCharArray(), newPassword.toCharArray(), true);
        } catch (SQLException e) {
            log.error("Can not change password", e);
            throw new RuntimeException("");
        }
        isUnlocked = true;
        applicationEventPublisher.publishEvent(new UnlockedEvent(""));
        isReady = true;
        applicationEventPublisher.publishEvent(new StoreReadyEvent(""));
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
            FileChannel fileChannel = new FileInputStream(files.stream().filter(f -> f.endsWith(Constants.SUFFIX_MV_FILE)).findFirst().get()).getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(ENCRYPT_HEADER.getBytes().length);
            fileChannel.read(buffer);
            buffer.flip();
            String res = new String(buffer.array());

            if (ENCRYPT_HEADER.equals(res)) {
                // Lock!
                log.warn("Database is locked, use 'unlock' command");
                return true;
            }
            log.debug("Database is unprotected");
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
