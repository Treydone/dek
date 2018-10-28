package fr.layer4.hhsl.registry;

public interface RegistryResolver {

    String getType();

    Registry prepare(RegistryConnection registryConnection);
}
