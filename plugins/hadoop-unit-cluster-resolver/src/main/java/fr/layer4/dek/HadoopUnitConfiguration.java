package fr.layer4.dek;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class HadoopUnitConfiguration {

    @Bean
    public YamlPropertiesFactoryBean hadoopUnitProperties(ResourceLoader resourceLoader) {
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(
                resourceLoader.getResource("classpath:hdp.matrix.yml"),
                resourceLoader.getResource("classpath:hadoop-unit.matrix.yml"));
        return yamlPropertiesFactoryBean;
    }

}
