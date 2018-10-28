package fr.layer4.hhsl.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClusterInfoManager {

    @Autowired(required = false)
    private List<ClusterInfoResolver> resolvers;

    public ClusterInfoResolver fromType(String type) {
        return this.resolvers.stream().filter(r -> type.equals(r.getType())).findFirst().get();
    }

    public Collection<String> getAvailableTypes() {
        return this.resolvers.stream().map(ClusterInfoResolver::getType).collect(Collectors.toList());
    }
}
