package fr.layer4.hhsl;

import java.util.Map;
import java.util.Optional;

public interface PropertyManager {

    /**
     * @return
     */
    Map<String, String> getProperty();

    /**
     * Return the value associated by the key in the configuration.
     *
     * @param key
     * @return
     */
    Optional<String> getProperty(String key);

    /**
     * @param key
     * @param value
     */
    void setProperty(String key, String value);

    /**
     * Delete a key with its associated value.
     *
     * @param key
     */
    void deleteProperty(String key);
}
