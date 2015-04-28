package org.cbir.retrieval.web.rest.dto;

import retrieval.dist.ResultsSimilarities;
import retrieval.storage.index.ResultSim;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
public class ResultsJSON {

    ResultsSimilarities resultsSimilarities;

    Long id;

    public ResultsJSON(Long id,ResultsSimilarities resultsSimilarities) {
        this.id = id;
        this.resultsSimilarities = resultsSimilarities;
    }
    public Long getId() {
        return id;
    }

    public List<Map<String,Object>> getData() {
        return resultsSimilarities
            .getResults()
            .stream().map(
                this::createResult
            ).collect( Collectors.toList() );
    }



    private Map<String,Object> createResult(ResultSim rs) {
        Map<String,Object> result = new TreeMap<>();
        result.put("id",rs.getId()+"");
        result.put("properties",rs.getProperties());
        result.put("similarities",rs.getSimilarities());
        return result;
    }

}
