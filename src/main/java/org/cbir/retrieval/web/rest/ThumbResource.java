package org.cbir.retrieval.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.cbir.retrieval.security.AuthoritiesConstants;
import org.cbir.retrieval.service.RetrievalService;
import org.cbir.retrieval.service.StoreImageService;
import org.cbir.retrieval.service.exception.*;
import org.cbir.retrieval.web.rest.dto.ResultsJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import retrieval.client.RetrievalClient;
import retrieval.dist.ResultsSimilarities;
import retrieval.server.RetrievalServer;
import retrieval.storage.Storage;
import retrieval.storage.exception.AlreadyIndexedException;
import retrieval.storage.exception.NoValidPictureException;
import retrieval.utils.SizeUtils;

import javax.annotation.security.RolesAllowed;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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
