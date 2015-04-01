package org.cbir.retrieval.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.apache.commons.io.IOUtils;
import org.cbir.retrieval.security.AuthoritiesConstants;
import org.cbir.retrieval.service.RetrievalService;
import org.cbir.retrieval.service.StoreImageService;
import org.cbir.retrieval.service.exception.*;
import org.cbir.retrieval.web.rest.dto.ResultsJSON;
import org.cbir.retrieval.web.rest.dto.StorageJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
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
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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
@RequestMapping("/api")
public class ImageResource {

    private final Logger log = LoggerFactory.getLogger(ImageResource.class);

    @Inject
    private RetrievalService retrievalService;

    @Inject
    private StoreImageService storeImageService;

    @RequestMapping(value = "/storages/{storage}/images/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    ResponseEntity<Map> getByStorage(@PathVariable Long id, @PathVariable String storage) throws ResourceNotFoundException {
        log.debug("REST request to get image : {}");

        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();

        Storage storageImage = retrievalServer.getStorage(storage);
        if (storageImage == null)
            throw new ResourceNotFoundException("Storage " + storage + " cannot be found!");

        Map<String, String> properties = storageImage.getProperties(id);
        if (properties == null)
            throw new ResourceNotFoundException("Image " + id + " cannot be found on storage " + storage + " !");

        return new ResponseEntity<>(properties, HttpStatus.OK);
    }

    @RequestMapping(value = "/images/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    ResponseEntity<Map> get(@PathVariable Long id) throws ResourceNotFoundException {
        log.debug("REST request to get image : " + id);

        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();

        List<Map<String, String>> properties =
            retrievalServer.getStorageList()
                .parallelStream()
                .map(x -> x.getProperties(id))
                .filter(x -> x != null && !x.isEmpty())
                .collect(Collectors.toList());

        log.info(properties.toString());

        if (properties.isEmpty())
            throw new ResourceNotFoundException("Image " + id + " cannot be found !");

        return new ResponseEntity<>(properties.get(0), HttpStatus.OK);
    }


    @RequestMapping(value = "/storages/{storage}/images",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    ResponseEntity<List> getAllByStorage(@PathVariable String storage) throws CBIRException {
        log.debug("REST request to list images : {}");

        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();
        Storage storageImage = retrievalServer.getStorage(storage);
        if (storageImage == null)
            throw new ResourceNotFoundException("Storage " + storage + " cannot be found!");

        try {
            List<Map<String, String>> map = storageImage.getAllPicturesMap()
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
            return new ResponseEntity<>(map, HttpStatus.OK);
        } catch (Exception e) {
            throw new CBIRException(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/images",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    ResponseEntity<List> getAll() throws CBIRException {
        log.debug("REST request to list images : {}");

        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();

        try {
            List<Map<String, String>> map = retrievalServer.getInfos()
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
            return new ResponseEntity<>(map, HttpStatus.OK);
        } catch (Exception e) {
            throw new CBIRException(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/images",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public ResponseEntity<Map<String, String>> create(
        @RequestParam(value = "id", required = false) Long idImage,
        @RequestParam(value = "storage", required = false) String idStorage,
        @RequestParam(required = false) String keys,
        @RequestParam(required = false) String values,
        @RequestParam(defaultValue = "false") Boolean async,//http://stackoverflow.com/questions/17693435/how-to-give-default-date-values-in-requestparam-in-spring
        //MultipartHttpServletRequest request
        @RequestParam("file") MultipartFile file
    ) throws CBIRException, IOException {
        log.debug("REST request to create image : {}", idImage);
        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();

        BufferedImage image;
        try {
            byte[] data = file.getBytes();
            image = ImageIO.read(new ByteArrayInputStream(data));
        } catch (IOException ex) {
            throw new ResourceNotValidException("Image not valid:" + ex.toString());
        }

        Map<String, String> result = indexPicture(idImage, idStorage, keys, values, async, image, retrievalServer,true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @RequestMapping(value = "/storages/{storage}/images/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    public void delete(
        @PathVariable Long id,
        @PathVariable String storage
    ) throws Exception {
        log.debug("REST request to delete storage : {}", id);
        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();

        Storage storageImage = retrievalServer.getStorage(storage);
        if (storageImage == null)
            throw new ResourceNotFoundException("Storage " + storage + " cannot be found!");

        if (!storageImage.isPictureInIndex(id))
            throw new ResourceNotFoundException("Image " + id + " cannot be found on storage " + storage + " !");

        try {
            storageImage.deletePicture(id);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CBIRException("Cannot delete image:" + e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/search",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    ResponseEntity<ResultsJSON> search(
        @RequestParam(defaultValue = "30") Integer max,
        @RequestParam(defaultValue = "") String storages,
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "saveImage",defaultValue = "false") Boolean saveimage
    ) throws CBIRException, IOException {
        log.debug("REST request to get CBIR results : max=" + max + " storages=" + storages);
        BufferedImage image = null;
        try {
            System.out.println(file.getOriginalFilename());
            byte[] data = file.getBytes();
            image = ImageIO.read(new ByteArrayInputStream(data));
        } catch (IOException ex) {
            throw new ResourceNotValidException("Image not valid:" + ex.toString());
        }
        log.info(storeImageService + " " );
        return doSearchSim(max, storages, image,saveimage);
    }


    @RequestMapping(value = "/searchUrl",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    ResponseEntity<ResultsJSON> search(
        @RequestParam(defaultValue = "30") Integer max,
        @RequestParam(defaultValue = "") String storages,
        @RequestParam(required = false) String url,
        @RequestParam(required = false) Long id,
        @RequestParam(value = "saveImage",defaultValue = "false") Boolean saveimage
    ) throws CBIRException, IOException {
        log.debug("REST request to get CBIR results : max=" + max + " url=" + url + " id=" +id+" storages=" + storages);
        BufferedImage image = null;

        try {

            if(id!=null) {
                image = storeImageService.tryReadIndexImage(id);
            }

            //if no id or id has no valid images
            if(image==null) {
                image = ImageIO.read(new URL(url));
            }

        } catch (IOException ex) {
            throw new ResourceNotValidException("Image not valid:" + ex.toString());
        }
        log.info(storeImageService + " " );
        return doSearchSim(max, storages, image,saveimage);
    }


    @RequestMapping(value = "/index/file",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public void indexFile(
        @RequestParam(required = true) String file,
        @RequestParam(required = false,defaultValue = "false") Boolean async
    ) throws CBIRException, IOException {
        log.debug("REST request to INDEX FULL : file="+file + " async="+async);
        log.debug("REST request to INDEX FULL : file="+new File(file).getAbsolutePath());
            StringBuffer jsonString = new StringBuffer();

            FileReader fr = new FileReader(new File(file));
            BufferedReader br = new BufferedReader(fr);
            String line;
            while((line = br.readLine()) != null){
                jsonString.append(line);
            }
            br.close();
            fr.close();

        List<Object> list = new JacksonJsonParser().parseList(jsonString.toString());

        for(Object entry : list) {
            try {
                Map map = (Map) entry;//new JacksonJsonParser().parseMap((String)entry);
                Long id = Long.parseLong(map.get("id").toString());
                String storage = map.get("storage").toString();
                String cropURL = map.get("url").toString();
                //read images if on disk
                boolean saveImage = true;
                BufferedImage image = null;
                try {
                    image = storeImageService.readIndexImage(id);
                    saveImage = false; //don't save image, already on disk!
                } catch (Exception e) {
                    image = ImageIO.read(new URL(cropURL));
                }
                indexPicture(id, storage, "", "", async, image, retrievalService.getRetrievalServer(),saveImage);
            } catch(Exception e) {
                log.error(e.toString());
            }
        }
    }

    @RequestMapping(value = "/index/full",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public void indexFull(
        @RequestBody String json
    ) throws CBIRException, IOException {
        log.debug("REST request to INDEX FULL : json="+json);
        List<Object> list = new JacksonJsonParser().parseList(json);

        log.debug(list.toString());
        for(Object entry : list) {
            try {
                Map map = (Map) entry;//new JacksonJsonParser().parseMap((String)entry);
                Long id = Long.parseLong(map.get("id").toString());
                String storage = map.get("storage").toString();
                String cropURL = map.get("url").toString();
                //read images if on disk
                boolean saveImage = true;
                BufferedImage image = null;
                try {
                    image = storeImageService.readIndexImage(id);
                    saveImage = false; //don't save image, already on disk!
                } catch (Exception e) {
                    image = ImageIO.read(new URL(cropURL));
                }
                indexPicture(id, storage, "", "", false, image, retrievalService.getRetrievalServer(),saveImage);
            } catch(Exception e) {
                log.error(e.toString());
            }
        }
    }

    private ResponseEntity<ResultsJSON> doSearchSim(Integer max, String storages, BufferedImage image, Boolean saveImage) throws ResourceNotValidException, IOException {
        String[] storagesArray = new String[0];
        if (!storages.isEmpty()) {
            storagesArray = storages.split(";");
        }

        Long searchId = new Date().getTime() + new Random().nextInt(100);

        RetrievalClient retrievalClient = retrievalService.getRetrievalClient();
        ResultsSimilarities rs = null;
        try {
            rs = retrievalClient.search(image, max, storagesArray);
            log.info(storeImageService + " " + searchId);
            if(saveImage) {
                storeImageService.saveSearchImage(searchId,image);
            }
        } catch (retrieval.exception.CBIRException e) {
            throw new ResourceNotValidException("Cannot process CBIR request:" + e);
        }


        return new ResponseEntity<>(new ResultsJSON(searchId,rs), HttpStatus.OK);
    }

    private Map<String, String> indexPicture(Long idImage, String idStorage, String keys, String values, Boolean async, BufferedImage image, RetrievalServer retrievalServer, boolean saveImage) throws CBIRException, IOException {
        Storage storage;
        if (idStorage == null) {
            storage = retrievalServer.getNextStorage();
        } else {
            storage = retrievalServer.getStorage(idStorage, true);
        }

        Map<String, String> properties = new TreeMap<>();
        if (keys != null) {
            String[] keysArray = keys.split(";");
            String[] valuesArray = values.split(";");

            if (keysArray.length != valuesArray.length) {
                throw new ParamsNotValidException("keys.size()!=values.size()");
            }

            for (int i = 0; i < keysArray.length; i++) {
                properties.put(keysArray[i], valuesArray[i]);
            }
        }

        //index picture
        Long id;
        try {
            if (async) {
                id = storage.addToIndexQueue(image, idImage, properties);
            } else {
                id = storage.indexPicture(image, idImage, properties);
            }
        } catch (AlreadyIndexedException e) {
            throw new ResourceAlreadyExistException("Image " + idImage + "already exist in storage " + idStorage);
        } catch (NoValidPictureException e) {
            throw new ResourceNotValidException("Cannot insert image:" + e.toString());
        } catch (Exception e) {
            throw new CBIRException("Cannot insert image:" + e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if(saveImage) {
            storeImageService.saveIndexImage(id, image);
        }

        return storage.getProperties(id);
    }

}
