package com.kevin.sample.es.router;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.kevin.sample.es.domain.Demo;
import com.origin.starter.web.OriginWebApplication;
import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.domain.OriginVertxContext;
import com.origin.starter.web.spi.OriginRouter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.Function;

@Slf4j
public class DataRouter implements OriginRouter {

    @Override
    public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {
        originConfig.getEventBus().consumer("data")
                .handler(ar -> {
                    try {
                        setDataV2(originVertxContext, "data", Long.parseLong(ar.body().toString()), v -> null);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

    }


    private void setDataV2(OriginVertxContext originVertxContext, String index, Long o, Function<Void, Void> handleData) throws IOException {

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
            e.printStackTrace();
        }


    }


}
