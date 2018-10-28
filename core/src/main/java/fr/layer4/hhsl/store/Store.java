package fr.layer4.hhsl.store;

public interface Store {

    /**
     * Initialize a clear store.
     */
    void init();

    /**
     * Clear all the data and metadata in the store.
     */
    void purge();

    boolean isReady();

}
