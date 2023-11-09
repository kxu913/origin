package com.origin.starter.web.domain;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.web.Router;
import io.vertx.micrometer.PrometheusScrapingHandler;
import lombok.Data;


@Data
public class OriginVertxContext {

    private HttpServer server;
    private Router router;
    private FileSystem fs;
    private Vertx vertx;


    public OriginVertxContext fromVertx(Vertx vertx) {
        this.server = vertx.createHttpServer();
        this.router = Router.router(vertx);
        this.fs = vertx.fileSystem();
        this.vertx = vertx;
        initRouter(vertx);
        return this;
    }

    private void initRouter(Vertx vertx) {
        this.router.route("/metrics").handler(PrometheusScrapingHandler.create());
        this.router.route("/health*").handler(HealthCheckHandler.create(vertx));
        this.router.errorHandler(HttpResponseStatus.SERVICE_UNAVAILABLE.code(), ctx -> {
            ctx.end("something went to wrong, try it later.");
        });
        this.server.requestHandler(this.router);
    }


}
