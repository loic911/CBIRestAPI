package org.cbir.retrieval.service;

import org.cbir.retrieval.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the UserResource REST controller.
 *
 * @see org.cbir.retrieval.service.UserService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@Transactional
public class StoreImageServiceTest {

    @Inject
    private StoreImageService storeImageService;

    @Test
    public void testSaveThumb() throws IOException {
        File img = new File("testdata/cbir.png");
        assertThat(img.exists()).isTrue();
        File file = storeImageService.saveIndexImage(123l, ImageIO.read(img));
        assertThat(file.exists()).isTrue();

        BufferedImage image = storeImageService.readIndexImage(123l);
        assertThat(image).isNotNull();
    }


//    @Test(expected=IOException.class)
//    public void testReadThumbNotExist() throws IOException {
//        BufferedImage image = storeImageService.readIndexImage(999999l);
//    }
}
