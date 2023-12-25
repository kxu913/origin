package com.origin.starter.web.router;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.spi.ESData;
import com.origin.framework.spi.OriginRouter;
import com.origin.starter.common.data.ESDataProcess;
import com.origin.starter.web.OriginWebApplication;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;

import java.util.List;
import java.util.ServiceLoader;

@Slf4j
public class ESDataRouter implements OriginRouter {


    @Override
    public void router(OriginWebVertxContext originVertxContext, OriginConfig originConfig) {

        ServiceLoader<ESData> loader = ServiceLoader.load(ESData.class);

        loader.forEach(esData -> ESDataProcess.process(originConfig, esData, this::batchSetData));


    }

    private void batchSetData(List<ESData> data) {

        try (RestClient restClient = OriginWebApplication.getBeanFactory().getESRestClient();

             ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper())
        ) {
            ESDataProcess.handleListData(transport, data);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

}
