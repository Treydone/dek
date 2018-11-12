package fr.layer4.hhsl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class HadoopUnitConfiguration {

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public YamlPropertiesFactoryBean hadoopUnitProperties() {
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(
                this.resourceLoader.getResource("classpath:hdp.matrix.yml"),
                this.resourceLoader.getResource("classpath:hadoop-unit.matrix.yml"));
        return yamlPropertiesFactoryBean;
    }

}
