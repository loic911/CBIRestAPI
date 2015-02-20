package org.cbir.retrieval.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrieval.client.RetrievalClient;
import retrieval.config.ConfigClient;
import retrieval.config.ConfigServer;
import retrieval.server.RetrievalServer;
import javax.servlet.ServletContext;

/**
 * Service class for managing retrieval server.
 */
@Service
@Transactional
public class RetrievalService {

    private final Logger log = LoggerFactory.getLogger(RetrievalService.class);

    @Autowired
    ServletContext servletContext;

    public void initRetrievalServer() throws Exception{
        log.info("Init retrieval server");
        RetrievalServer server = buildRetrievalServer();
        servletContext.setAttribute("server",server);
        servletContext.setAttribute("client",buildRetrievalClient(server));
    }

    public RetrievalServer getRetrievalServer() {
        return (RetrievalServer)servletContext.getAttribute("server");
    }



    public RetrievalServer buildRetrievalServer() throws Exception {
        ConfigServer configServer = new ConfigServer("config/ConfigServer.prop");
        configServer.setStoreName("MEMORY");
        RetrievalServer server = new RetrievalServer(configServer,"cbir",false);
        server.createStorage("default");
        return server;
    }

    public RetrievalClient buildRetrievalClient(RetrievalServer server) throws Exception {
        return new RetrievalClient(new ConfigClient("config/ConfigClient.prop"),server);
    }

}
