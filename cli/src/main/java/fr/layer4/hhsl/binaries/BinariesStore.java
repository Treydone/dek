package fr.layer4.hhsl.binaries;

public interface BinariesStore {

    /**
     * Download and unpack an archive.
     *
     * @param url
     */
    void prepare(String repository, String url);
}
