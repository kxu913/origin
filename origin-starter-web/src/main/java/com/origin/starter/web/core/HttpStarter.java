package com.origin.starter.web.core;


import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginWebVertxContext;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpStarter {

    private final OriginConfig originConfig;
    private final OriginWebVertxContext vertxContext;

    public HttpStarter(OriginWebVertxContext vertxContext, OriginConfig originConfig) {
        this.originConfig = originConfig;
        this.vertxContext = vertxContext;

    }

    public Future<Void> startHttpServer() {
        return Future.future(ar -> {
            originConfig.getRetriever().getConfig()
                    .onComplete(aServerConfig -> {
                        if (aServerConfig.succeeded()) {
                            JsonObject serverConfig = aServerConfig.result().getJsonObject("server");
                            log.info("{}",serverConfig);
                            int port = serverConfig.getInteger("port");
                            vertxContext.getServer().listen(port)
                                    .onComplete(sar -> {
                                        if (sar.succeeded()) {
                                            log.info("**** server started at {}", port);
                                            ar.complete();
                                        } else {
                                            log.error("server started failed.", sar.cause());
                                            ar.fail(sar.cause());
                                        }

                                    })
                                    .onFailure(err -> {
                                        log.error("server started failed.", err);
                                        ar.fail(err);
                                    });
                        } else {
                            log.error("http server start failed, due to config incorrect.", aServerConfig.cause());
                            ar.fail(aServerConfig.cause());
                        }

                    })
                    .onFailure(err -> {
                        log.error(err.getMessage(), err);
                        ar.fail(err);
                    });
        });
    }
}
