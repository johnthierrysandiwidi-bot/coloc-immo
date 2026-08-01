package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.ImageTestSamples.*;
import static bf.colocation.immo.domain.ImmobilierTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ImageTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Image.class);
        Image image1 = getImageSample1();
        Image image2 = new Image();
        assertThat(image1).isNotEqualTo(image2);

        image2.setId(image1.getId());
        assertThat(image1).isEqualTo(image2);

        image2 = getImageSample2();
        assertThat(image1).isNotEqualTo(image2);
    }

    @Test
    void immobilierTest() {
        Image image = getImageRandomSampleGenerator();
        Immobilier immobilierBack = getImmobilierRandomSampleGenerator();

        image.setImmobilier(immobilierBack);
        assertThat(image.getImmobilier()).isEqualTo(immobilierBack);

        image.immobilier(null);
        assertThat(image.getImmobilier()).isNull();
    }
}
