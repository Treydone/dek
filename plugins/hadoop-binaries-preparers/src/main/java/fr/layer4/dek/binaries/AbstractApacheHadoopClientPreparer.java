package fr.layer4.dek.binaries;

import fr.layer4.dek.DekException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public abstract class AbstractApacheHadoopClientPreparer extends AbstractApacheClientPreparer {

    public AbstractApacheHadoopClientPreparer(CloseableHttpClient client, ApacheMirrorFinder apacheMirrorFinder) {
        super(client, apacheMirrorFinder);
    }

    protected File downloadClient(Path basePath, String version, boolean force, String nameAndVersion, String archive) {
        File dest = basePath.resolve(FilenameUtils.getBaseName(archive)).toFile();
        log.debug("Preparing {} to {}", archive, dest);

        // Check if archive if already present
        if (force || !basePath.resolve(archive).toFile().exists()) {
            download(basePath, version, archive);
        }

        // Unpack
        File source = basePath.resolve(archive).toFile();
        log.debug("Uncompress {} to {}", source, dest);
        if (force || !dest.exists()) {
            try {
                uncompress(source, dest);
            } catch (IOException e) {
                throw new DekException("Can not extract client", e);
            }
        }

        dest = new File(dest, nameAndVersion);
        return dest;
    }
}
