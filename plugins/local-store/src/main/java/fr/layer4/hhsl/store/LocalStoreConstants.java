package fr.layer4.hhsl.store;

public interface LocalStoreConstants {

    String HOME = "~/";
    String FOLDER = ".hhsl";

    static String getRootPath() {
        return LocalStoreConstants.HOME + LocalStoreConstants.FOLDER;
    }
}
