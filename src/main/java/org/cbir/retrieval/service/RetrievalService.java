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

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.io.File;
import java.util.*;

/**
 * Service class for managing retrieval server.
 */
@Service
@Transactional
public class RetrievalService {

    public static String DEFAULT_STORAGE = "default";
    public static String OTHER_STORAGE = "abc";

    private final Logger log = LoggerFactory.getLogger(RetrievalService.class);

    @Inject
    private Environment env;

    @Autowired
    ServletContext servletContext;

    public void initRetrievalServer() throws Exception{
        log.info("Init retrieval server");
        RetrievalServer server = null;
        String envir = env.getActiveProfiles()[0];
        log.info("profile is "+ Arrays.toString(env.getActiveProfiles()) +" => " +envir);
        if (envir.equals("test")) {
            server = buildRetrievalServerForTest();
        }
        if (envir.equals("dev")) {
            server = buildRetrievalServerForDev();
        }
        if (envir.equals("prod")) {
            server = buildRetrievalServerForDev();
        }

        servletContext.setAttribute("server",server);
        servletContext.setAttribute("client",buildRetrievalClient(server));
    }

    public RetrievalServer getRetrievalServer() {
        return (RetrievalServer)servletContext.getAttribute("server");
    }



    public RetrievalServer buildRetrievalServerForTest() throws Exception {
        ConfigServer configServer = new ConfigServer("config/ConfigServer.prop");
        configServer.setStoreName("MEMORY");
        RetrievalServer server = new RetrievalServer(configServer,"cbir",false);
        server.createStorage(DEFAULT_STORAGE);
        return server;
    }

    public RetrievalServer buildRetrievalServerForDev() throws Exception {
        ConfigServer configServer = new ConfigServer("config/ConfigServer.prop");
        configServer.setStoreName("MEMORY");
        RetrievalServer server = new RetrievalServer(configServer,"cbir",false);
        server.createStorage(DEFAULT_STORAGE);
        server.createStorage(OTHER_STORAGE);

        Map<String,String> properties = new TreeMap<>();
        properties.put("date",new Date().toString());

        server.getStorage(DEFAULT_STORAGE).indexPicture(ImageIO.read(new File("testdata/images/crop1.jpg")), 1l, new HashMap<>(properties));//should be fix in the retrieval lib?
        server.getStorage(DEFAULT_STORAGE).indexPicture(ImageIO.read(new File("testdata/images/crop2.jpg")), 2l, new HashMap<>(properties));
        server.getStorage(DEFAULT_STORAGE).indexPicture(ImageIO.read(new File("testdata/images/crop3.jpg")), 3l, new HashMap<>(properties));
        server.getStorage(OTHER_STORAGE).indexPicture(ImageIO.read(new File("testdata/images/crop4.jpg")), 4l, new HashMap<>(properties));

        return server;
    }

    public RetrievalClient buildRetrievalClient(RetrievalServer server) throws Exception {
        return new RetrievalClient(new ConfigClient("config/ConfigClient.prop"),server);
    }

    public void reset() throws Exception {
        initRetrievalServer();
    }

}
