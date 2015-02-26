package org.cbir.retrieval.web.rest.dto;

import retrieval.storage.Storage;

/**
 * Created by lrollus on 20/02/15.
 */
public class StorageJSON {

    String name;

    String id;

    Long size;

    Integer queueSize;

    public StorageJSON() {
    }

    public StorageJSON(Storage storage) {
        this.id = storage.getStorageName();
        this.name = storage.getStorageName();
        this.size = storage.getNumberOfItem();
        this.queueSize = storage.getIndexQueueSize();
    }

    public String getId() {
        return id;
    }

    public String getStorageName() {
        return name;
    }

    public Long getSize() {
        return size;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

}
