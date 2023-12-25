package com.origin.starter.common.data;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.spi.ESData;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Slf4j
public class ESDataProcess {

    public static void process(OriginConfig originConfig, ESData esData, Consumer<List<ESData>> func) {
        String queue = esData.queueName();
        int batchSize = esData.esBatchSize();
        List<ESData> esDataList = new ArrayList<>();
        log.info("start consume message from {}.", queue);
        originConfig.getEventBus().consumer(queue, message -> {
            String end = message.body().toString();
            if (end.equals("end")) {
                func.accept(esDataList);
                log.info("batch process reached end.");
                return;
            }
            ESData data = new JsonObject(message.body().toString()).mapTo(esData.getClass());
            esDataList.add(data);
            if (esDataList.size() == batchSize) {
                func.accept(esDataList);
                esDataList.clear();
            }
        });
    }

    public static void handleListData(ElasticsearchTransport transport, List<ESData> data) throws ExecutionException, InterruptedException {
        if (data.isEmpty()) {
            return;
        }
        String index = data.get(0).esIndex();
        ElasticsearchAsyncClient esClient =
                new ElasticsearchAsyncClient(transport);
        List<BulkOperation> operations = new ArrayList<>();

        data.forEach(gd -> {
            operations.add(new BulkOperation.Builder().create(d -> d.index(index).id(gd.getId()).document(gd)).build());
        });
        esClient.bulk(r -> {
            log.info("batch insert {}", data.size());
            return r.index(index).operations(operations);
        }).get();
    }
}
