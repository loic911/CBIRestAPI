package org.cbir.retrieval.service;
/*
 * Copyright (c) 2009-2015. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
//    public static String IMAGE_PATH_INDEX = "/data/thumb/index";
//    public static String IMAGE_PATH_SEARCH = "/data/thumb/search";
    public static boolean SAVE_IMAGE = true;
    public static String NO_IMAGE_FOUND_PATH = "images/nothumb.png";

    private final Logger log = LoggerFactory.getLogger(StoreImageService.class);

    @Inject
    private Environment env;

    public File saveIndexImage(Long id,BufferedImage image) throws IOException {
        return saveImage(id,image,env.getProperty("retrieval.thumb.index"));
    }

    public File saveSearchImage(Long id,BufferedImage image) throws IOException {
        return saveImage(id,image,env.getProperty("retrieval.thumb.search"));
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
        return readImage(id,env.getProperty("retrieval.thumb.index"));
    }

    public BufferedImage readSearchImage(Long id) throws IOException {
        return readImage(id,env.getProperty("retrieval.thumb.search"));
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

    public BufferedImage tryReadIndexImage(Long id) throws IOException {
        String path = env.getProperty("retrieval.thumb.index");
        File dir = new File(path);
        File file = new File(dir,id+".png");
        log.info("Read IMAGE in "+file.getAbsolutePath());
        if(file.exists()) {
            return ImageIO.read(file);
        }
        return null;
    }

}
