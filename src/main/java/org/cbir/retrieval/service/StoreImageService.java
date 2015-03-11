package org.cbir.retrieval.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Service class for managing image thumb.
 */
@Service
@Transactional
public class StoreImageService {

    //TODO: extract config external
    public static String IMAGE_PATH_INDEX = "thumb/index";
    public static String IMAGE_PATH_SEARCH = "thumb/search";
    public static boolean SAVE_IMAGE = true;
    public static String NO_IMAGE_FOUND_PATH = "images/nothumb.png";

    private final Logger log = LoggerFactory.getLogger(StoreImageService.class);

    @Inject
    private Environment env;

    public File saveIndexImage(Long id,BufferedImage image) throws IOException {
        return saveImage(id,image,IMAGE_PATH_INDEX);
    }

    public File saveSearchImage(Long id,BufferedImage image) throws IOException {
        return saveImage(id,image,IMAGE_PATH_SEARCH);
    }

    public File saveImage(Long id,BufferedImage image, String path) throws IOException {
        if(SAVE_IMAGE) {
            File dir = new File(path);
            log.info("Save IMAGE in "+dir.getAbsolutePath());

            if(!dir.exists()) {
                log.info("Create IMAGE dir "+dir.getAbsolutePath());
                dir.mkdirs();
            }

            File file = new File(dir,id+".png");
            ImageIO.write(image, "png", file);
            return file;
        }
        return null;
    }

    public BufferedImage readIndexImage(Long id) throws IOException {
        return readImage(id,IMAGE_PATH_INDEX);
    }

    public BufferedImage readSearchImage(Long id) throws IOException {
        return readImage(id,IMAGE_PATH_SEARCH);
    }

    private BufferedImage readImage(Long id,String path) throws IOException {
        File dir = new File(path);
        File file = new File(dir,id+".png");
        log.info("Read IMAGE in "+file.getAbsolutePath());

        if(!file.exists()) {
            file = new File(NO_IMAGE_FOUND_PATH);
            log.info("Read IMAGE: file does not exist, response:"+file.getAbsolutePath());
        }
        return ImageIO.read(file);
    }

}
