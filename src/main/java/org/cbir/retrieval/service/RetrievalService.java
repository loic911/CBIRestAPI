package org.cbir.retrieval.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrieval.client.RetrievalClient;
import retrieval.config.ConfigClient;
import retrieval.config.ConfigServer;
import retrieval.server.RetrievalServer;
import retrieval.storage.Storage;
import retrieval.storage.exception.AlreadyIndexedException;
import retrieval.storage.exception.NoValidPictureException;
import retrieval.storage.exception.PictureTooHomogeneous;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Service class for managing retrieval server.
 */
@Service
@Transactional
public class RetrievalService {

    public static String DEFAULT_TEST_STORAGE = "default";
    public static String DEFAULT_STORAGE = "dev";
    public static String OTHER_STORAGE = "abc";

    private final Logger log = LoggerFactory.getLogger(RetrievalService.class);

    @Inject
    private Environment env;

    @Inject
    private StoreImageService storeImageService;

    @Autowired
    ServletContext servletContext;

    public void initRetrievalServer() throws Exception{
        log.info("Init retrieval server");
        RetrievalServer server = null;
        String envir = "";
        if(env.getActiveProfiles().length>0) {
            envir = env.getActiveProfiles()[0];
            log.info("profile is "+ Arrays.toString(env.getActiveProfiles()) +" => " +envir);
            if (envir.equals("dev")) {
                server = buildRetrievalServerForDev();
            } else if (envir.equals("prod")) {
                server = buildRetrievalServerForProd();
            } else {
                server = buildRetrievalServerForTest();
            }
        } else server = buildRetrievalServerForTest();
        log.info("server "+ server);
        log.info("storages "+ server.getStorageList());

        servletContext.setAttribute("server",server);
        servletContext.setAttribute("client",buildRetrievalClient(server));
    }

    public RetrievalServer getRetrievalServer() {
        return (RetrievalServer)servletContext.getAttribute("server");
    }

    public RetrievalClient getRetrievalClient() {
        return (RetrievalClient)servletContext.getAttribute("client");
    }


    public RetrievalServer buildRetrievalServerForTest() throws Exception {
        ConfigServer configServer = new ConfigServer("config/ConfigServer.prop");
        configServer.setStoreName("MEMORY");
        RetrievalServer server = new RetrievalServer(configServer,"cbir",false);
        server.createStorage(DEFAULT_TEST_STORAGE);
        return server;
    }

    public RetrievalServer buildRetrievalServerForDev() throws Exception {
        ConfigServer configServer = new ConfigServer("config/ConfigServer.prop");
        configServer.setStoreName("REDIS");
        RetrievalServer server = new RetrievalServer(configServer,"cbir",false);

        if(configServer.getStoreName().equals("MEMORY")) {
            server.createStorage(DEFAULT_TEST_STORAGE);
            server.createStorage(OTHER_STORAGE);

            Map<String, String> properties = new TreeMap<>();
            properties.put("date", new Date().toString());

            indexPicture(server.getStorage(DEFAULT_TEST_STORAGE), ImageIO.read(new File("testdata/images/crop1.jpg")), 1l, new HashMap<>(properties));
            indexPicture(server.getStorage(DEFAULT_TEST_STORAGE), ImageIO.read(new File("testdata/images/crop2.jpg")), 2l, new HashMap<>(properties));
            indexPicture(server.getStorage(DEFAULT_TEST_STORAGE), ImageIO.read(new File("testdata/images/crop3.jpg")), 3l, new HashMap<>(properties));
            indexPicture(server.getStorage(OTHER_STORAGE), ImageIO.read(new File("testdata/images/crop4.jpg")), 4l, new HashMap<>(properties));
        }
        return server;
    }

    public RetrievalServer buildRetrievalServerForProd() throws Exception {
        ConfigServer configServer = new ConfigServer("config/ConfigServer.prop");
        configServer.setStoreName("MEMORY");
        RetrievalServer server = new RetrievalServer(configServer,"cbir",false);

        return server;
    }

    private void indexPicture(Storage storage,BufferedImage image,Long id, Map<String,String> properties) throws NoValidPictureException, AlreadyIndexedException, PictureTooHomogeneous, IOException {
        Long realId = storage.indexPicture(image,id,properties);
        storeImageService.saveIndexImage(realId,image);

    }


    public RetrievalClient buildRetrievalClient(RetrievalServer server) throws Exception {
        return new RetrievalClient(new ConfigClient("config/ConfigClient.prop"),server);
    }

    public void reset() throws Exception {
        initRetrievalServer();
    }

}
