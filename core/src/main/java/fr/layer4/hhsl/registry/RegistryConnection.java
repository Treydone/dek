package fr.layer4.hhsl.registry;

import lombok.Data;

import java.net.URI;
import java.util.Map;

/**
 * Represents a connection to registry. Connection are managed by the RegistryConnectionManager.
 */
@Data
public class RegistryConnection {
    private Long id;
    private String name;
    private URI uri;
    private Map<String, String> metadata;
}
