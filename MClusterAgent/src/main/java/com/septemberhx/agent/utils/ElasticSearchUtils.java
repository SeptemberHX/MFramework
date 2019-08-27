package com.septemberhx.agent.utils;

import com.septemberhx.common.log.MServiceBaseLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/27
 */
public class ElasticSearchUtils {

    private static Logger logger = LogManager.getLogger(ElasticSearchUtils.class);

    /**
     * Get the logs which are generated at the time between start time and end time.
     * The logs will be processed here to generate MServiceBaseLogs in order to reduce the data size.
     *
     * @param client: The rest high level client of elasticsearch
     * @param indices: The indices that the logs belong to
     * @param startTime: The start of the time range
     * @param endTime: The end of the time range
     * @return List: Service logs
     */
    public static List<MServiceBaseLog> getLogsBetween(RestHighLevelClient client, String[] indices, Date startTime, Date endTime) {
        List<MServiceBaseLog> logList = new ArrayList<>();
        SearchRequest sr = new SearchRequest(indices);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(startTime).to(endTime));
        sr.source(searchSourceBuilder);

        try {
            SearchResponse sq = client.search(sr, RequestOptions.DEFAULT);
            for (SearchHit hit : sq.getHits()) {
                logger.info(hit.getId());

                // todo: generate logs from the search result of elasticsearch and add it to the logList
            }
        } catch (IOException e) {
            logger.warn("Exception happens. Cannot fetch logs from elasticsearch!");
            logger.warn(e);
        }
        return logList;
    }

    public static void getLog(RestHighLevelClient client, String index, String id) {
        GetRequest rq = new GetRequest(index, id);
        try {
            GetResponse getResponse = client.get(rq, RequestOptions.DEFAULT);
            logger.debug(getResponse.getField("message"));
        } catch (IOException e) {
            logger.warn("Exception happens. Cannot get log from elasticsearch!");
            logger.warn(e);
        }
    }
}
