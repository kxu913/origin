package com.origin.starter.app.domain;


import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Data
@Slf4j
public class OriginAppConfig {
    private EventBus eventBus;
    private ConfigRetriever retriever;
    private JsonObject appConfig;

    public OriginAppConfig fromVertx(Vertx vertx) {
        this.eventBus = vertx.eventBus();
        ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions()
                .addStore(
                        new ConfigStoreOptions()
                                .setType("file")
                                .setConfig(new JsonObject().put("path", "conf/default_config.json")));
        try {
            retrieverOptions.addStore(new ConfigStoreOptions()
                    .setType("file")
                    .setConfig(new JsonObject().put("path", "conf/config.json")));

        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
        this.retriever = ConfigRetriever.create(vertx, retrieverOptions);

        return this;
    }

}
