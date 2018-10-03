package fr.layer4.hhsl.local;

import fr.layer4.hhsl.Utils;
import fr.layer4.hhsl.event.LockedEvent;
import fr.layer4.hhsl.event.StoreDestroyEvent;
import fr.layer4.hhsl.event.StoreReadyEvent;
import fr.layer4.hhsl.event.UnlockedEvent;
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
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * http://www.h2database.com/html/features.html#file_encryption
 */
@Slf4j
@Component
public class H2LocalStore implements LocalStore, InitializingBean, DisposableBean {

    public static final String CIPHER = "AES";
    public static final String DB = "local";
    public static final String FOLDER = ".hhsl";
    public static final String USER = "sa";
    public static final String PASSWORD = "sa";
    public static final String HOME = "~/";
    public static final String JDBC_H2 = "jdbc:h2:";
    public static final String CIPHER_EXTENSION = ";CIPHER=";
    public static final String ENCRYPT_HEADER = "H2encrypt\n";

    @Setter
    @Autowired
    private Utils utils;

    @Setter
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private JdbcConnectionPool dataSource;

    private boolean isUnlocked = false;
    private boolean isReady = false;

    public void purge() {
        if (dataSource != null) {
            log.debug("Dispose database");
            dataSource.dispose();
        }
        log.debug("Clean local file");
        DeleteDbFiles.execute(getRootPath(), DB, true);
        isReady = false;
        applicationEventPublisher.publishEvent(new StoreDestroyEvent(""));
        isUnlocked = false;
        applicationEventPublisher.publishEvent(new LockedEvent(""));
    }

    @Override
    public void afterPropertiesSet() {
        log.debug("Checking if database exists in {}...", getRootPath());
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
            log.warn("Database is locked, use 'unlock' command");
            isUnlocked = false;
            applicationEventPublisher.publishEvent(new LockedEvent(""));
            return;
        }
        log.debug("Database is unprotected");
        isUnlocked = true;
        applicationEventPublisher.publishEvent(new UnlockedEvent(""));
    }

    protected List<String> getDatabaseFiles() {
        List<String> files = FileLister.getDatabaseFiles(getRootPath(), DB, true);
        try {
            FileLister.tryUnlockDatabase(files, "encryption");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        files = FileLister.getDatabaseFiles(getRootPath(), DB, false);
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

    @Override
    public void unlock() {
        String password = utils.promptForPassword();

        List<String> databaseFiles = getDatabaseFiles();
        boolean encrypted = isEncrypted(databaseFiles);

        if (encrypted) {
            password += " " + PASSWORD;
        }

        dataSource = JdbcConnectionPool.create(JDBC_H2 + getDatabasePath() + CIPHER_EXTENSION + CIPHER, USER, password);
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
    public void init(boolean secured) {
        // Prepare location of database
        purge();

        String extension = "";
        String password = PASSWORD;
        // Ask for root password if needed
        if (secured) {
            password = utils.doublePromptForPassword() + " " + PASSWORD;
            extension += CIPHER_EXTENSION + CIPHER;
        }

        log.info("Create database (secured:{})", secured);
        dataSource = JdbcConnectionPool.create(JDBC_H2 + getDatabasePath() + extension, USER, password);

        isReady = true;
        applicationEventPublisher.publishEvent(new StoreReadyEvent(""));
        isUnlocked = true;
        applicationEventPublisher.publishEvent(new UnlockedEvent(""));

        log.debug("Create tables");
        try (
                Connection connection = dataSource.getConnection();
                // TODO
                PreparedStatement statement = connection.prepareStatement("create table test(id int primary key, name varchar(255))")) {
            statement.execute();
        } catch (SQLException e) {
            log.error("Can not create reference table", e);
            throw new RuntimeException(e);
        }
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

        String actualPassword = utils.promptForPassword();

        // Test connection with provided password
        try {
            DriverManager.getConnection(JDBC_H2 + getDatabasePath() + CIPHER_EXTENSION + CIPHER, USER, actualPassword);
        } catch (SQLException e) {
            log.error("Oups", e);
            throw new RuntimeException("");
        }

        String newPassword = utils.doublePromptForPassword();

        try {
            ChangeFileEncryption.execute(getRootPath(), DB, CIPHER, actualPassword.toCharArray(), newPassword.toCharArray(), true);
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
        return getRootPath() + "/" + DB;
    }

    protected static String getRootPath() {
        return HOME + FOLDER;
    }
}
