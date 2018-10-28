package fr.layer4.hhsl.binaries;

import org.springframework.stereotype.Component;

@Component
public class LocalBinariesStore implements BinariesStore {

    public static final String ARCHIVES = "archives";

    @Override
    public void prepare(String repository, String url) {

    }
}
