package org.cbir.retrieval.web.rest.dto;

import retrieval.dist.ResultsSimilarities;
import retrieval.storage.index.ResultSim;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Created by lrollus on 20/02/15.
 */
public class ResultsJSON {

    ResultsSimilarities resultsSimilarities;

    Long id;

    public ResultsJSON(Long id,ResultsSimilarities resultsSimilarities) {
        this.id = id;
        this.resultsSimilarities = resultsSimilarities;
    }
//
//    public Long getTotalSize() {
//        return (long)resultsSimilarities.getTotalSize();
//    }

//    public <T> List<List<T>> collate( List<T> list, int size, int step ) {
//        return Stream.iterate(0, i -> i + step)
//            .limit((list.size() / step) + 1)
//            .map(i -> list.stream()
//                .skip( i )
//                .limit( size )
//                .collect( Collectors.toList() ) )
//            .filter( i -> !i.isEmpty() )
//            .collect( Collectors.toList() ) ;
//    }

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
