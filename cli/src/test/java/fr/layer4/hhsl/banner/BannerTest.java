package fr.layer4.hhsl.banner;

import fr.layer4.hhsl.Cluster;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class BannerTest {

    @Test
    public void render_defaultBanner() {

        // Given
        Cluster cluster = new Cluster();
        cluster.setName("Test Cluster HDP");
        Banner banner = new Banner(Banner.DEFAULT_BANNER, cluster);

        // When
        CharSequence render = banner.render(8);

        // Then
        Assertions.assertThat(render).isEqualTo("--------\nUsing Test Cluster HDP\n--------");

    }
}
