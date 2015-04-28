package org.cbir.retrieval.web.rest.dto;

import retrieval.storage.Storage;
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
