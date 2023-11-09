package com.origin.starter.tools.core;

import com.origin.starter.tools.spi.OriginTool;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.micrometer.PrometheusScrapingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

@Slf4j
public class ToolsLoader {
    private final Vertx vertx;
    private final ConfigRetriever retriever;

    public ToolsLoader(Vertx vertx) {

        this.vertx = vertx;
        this.retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
                .addStore(
                        new ConfigStoreOptions()
                                .setType("file")
                                .setConfig(new JsonObject().put("path", "conf/default_config.json")))
                .addStore(new ConfigStoreOptions()
                        .setType("file")
                        .setConfig(new JsonObject().put("path", "conf/config.json"))));
    }

    public void load() {
        this.retriever.getConfig().onComplete(ar -> {

            if (ar.succeeded()) {
                JsonObject config = ar.result();
                JsonObject serverConfig = config.getJsonObject("server");
                HttpServer httpServer = vertx.createHttpServer();
                Router router = Router.router(vertx);
                int port = serverConfig.getInteger("port");
                initRouter(router);
                httpServer.requestHandler(router).listen(port).onComplete(xar -> {
                    if (xar.succeeded()) {
                        log.info("tool run at {}", port);
                        ServiceLoader<OriginTool> loader = ServiceLoader.load(OriginTool.class);
                        OriginVertxContext context = createContext(httpServer, router);
                        loader.forEach(originTool -> {
                            String key = originTool.getKey();
                            if (config.containsKey(key)) {
                                initTool(originTool, context, config.getJsonObject(key));
                            }
                        });
                    }
                }).onFailure(err -> {
                    log.error("tool run error", err);
                });

            }

        }).onFailure(err -> log.error("{}", err.getMessage(), err));
    }

    private void initTool(OriginTool originTool, OriginVertxContext context, JsonObject config) {
        originTool.setOriginVertxContent(context);
        originTool.setOriginConfig(config);
        originTool.init();
    }

    private OriginVertxContext createContext(HttpServer httpServer, Router router) {
        OriginVertxContext context = new OriginVertxContext()
                .withVertx(vertx)
                .withHttpServer(httpServer)
                .withRouter(router);
        log.info("context {} had been created.", context);
        return context;
    }

    private void initRouter(Router router) {
        router.route("/metrics").handler(PrometheusScrapingHandler.create());
        router.route("/health*").handler(HealthCheckHandler.create(this.vertx));
        router.route().handler(BodyHandler.create());
        router.errorHandler(HttpResponseStatus.SERVICE_UNAVAILABLE.code(), ctx -> {
            ctx.end("something went to wrong, try it later.");
        });
    }
}
