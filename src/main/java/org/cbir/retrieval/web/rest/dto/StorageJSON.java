package org.cbir.retrieval.web.rest.dto;

import retrieval.storage.Storage;

/**
 * Created by lrollus on 20/02/15.
 */
public class StorageJSON {

    Storage storage;

    public StorageJSON(Storage storage) {
        this.storage = storage;
    }

    public String getId() {
        return storage.getStorageName();
    }

    public String getStorageName() {
        return storage.getStorageName();
    }

    public Long getSize() {
        return storage.getNumberOfItem();
    }

    public Integer getQueueSize() {
        return storage.getIndexQueueSize();
    }

}
