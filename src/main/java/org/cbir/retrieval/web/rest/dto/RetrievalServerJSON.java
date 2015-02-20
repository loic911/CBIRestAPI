package org.cbir.retrieval.web.rest.dto;

import retrieval.server.RetrievalServer;

import java.io.Serializable;

/**
 * Created by lrollus on 20/02/15.
 */
public class RetrievalServerJSON implements Serializable {

    RetrievalServer server;

    public RetrievalServerJSON(RetrievalServer server) {
        this.server = server;
    }

    public String getHost() {
        return "localhost";
    }

    public int getPort() {
        return server.getPort();
    }

    public long getSize() {
        return server.getSize();
    }

}
