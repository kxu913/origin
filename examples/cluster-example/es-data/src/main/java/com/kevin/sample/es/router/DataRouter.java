package com.kevin.sample.es.router;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.kevin.sample.es.domain.Demo;
import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.spi.OriginRouter;
import com.origin.starter.web.OriginWebApplication;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.Function;

@Slf4j
public class DataRouter implements OriginRouter {

    @Override
    public void router(OriginWebVertxContext originVertxContext, OriginConfig originConfig) {
        originConfig.getEventBus().consumer("data")
                .handler(ar -> {
                    try {
                        setDataV2(originVertxContext, "data", Long.parseLong(ar.body().toString()), v -> null);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

    }


    private void setDataV2(OriginWebVertxContext originVertxContext, String index, Long o, Function<Void, Void> handleData) throws IOException {

        try (
                RestClient restClient = OriginWebApplication.getBeanFactory().getESRestClient();
                ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ) {
            ElasticsearchAsyncClient esClient =
                    new ElasticsearchAsyncClient(transport);
            System.out.println(o);
            esClient.index(i -> {
                String dt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
                Demo demo = new Demo();
                demo.setId(o);
                demo.setDate(new Date());
                i.index(index).id(o.toString()).document(demo);
                return i;
            }).get();


        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


    }


}
