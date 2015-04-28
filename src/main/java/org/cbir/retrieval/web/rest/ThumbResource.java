package org.cbir.retrieval.web.rest;
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
import com.codahale.metrics.annotation.Timed;
import org.cbir.retrieval.security.AuthoritiesConstants;
import org.cbir.retrieval.service.RetrievalService;
import org.cbir.retrieval.service.StoreImageService;
import org.cbir.retrieval.service.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retrieval.storage.exception.NoValidPictureException;
import retrieval.utils.SizeUtils;

import javax.annotation.security.RolesAllowed;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * REST controller for managing images.
 */
@RestController
@RequestMapping("/thumb")
public class ThumbResource {

    private final Logger log = LoggerFactory.getLogger(ThumbResource.class);

    @Inject
    private RetrievalService retrievalService;

    @Inject
    private StoreImageService storeImageService;

    @RequestMapping(value = "/{id}",
        method = RequestMethod.GET,
        produces = MediaType.IMAGE_PNG_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ANONYMOUS)
    ResponseEntity<byte[]> getStoreImage(@PathVariable Long id,@RequestParam(required = false) Integer size,@RequestParam(required = false,defaultValue = "index") String type) throws ResourceNotFoundException, NoValidPictureException {
        log.debug("REST request to get image thumb : " + id);

        try {
            BufferedImage image = null;
            if(type.equals("index")) {
                image = storeImageService.readIndexImage(id);
            } else {
                image = storeImageService.readSearchImage(id);
            }

            if(size!=null) {
                SizeUtils sizeCompute = new SizeUtils(image.getWidth(),image.getHeight(),size,size);
                sizeCompute = sizeCompute.computeThumbSize();
                //resize image
                image = resizePicture(image,sizeCompute.getWidth(),sizeCompute.getHeight());
            }


            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write( image, "png", baos );
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return new ResponseEntity<>(imageInByte, headers, HttpStatus.OK);
        } catch(IOException e) {
            throw new NoValidPictureException("Image with "+id + "cannot be read");
        }
    }

    private static BufferedImage resizePicture(BufferedImage image, int targetWidth, int targetHeight) {
        int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, type);
        Graphics2D g = resizedImage.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;

    }
}
