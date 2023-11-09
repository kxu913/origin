package com.origin.starter.tools.core;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.micrometer.PrometheusScrapingHandler;
import lombok.Data;


@Data
public class OriginVertxContext {
    private Vertx vertx;
    private HttpServer server;
    private Router router;
    private FileSystem fs;


    public OriginVertxContext withVertx(Vertx vertx) {
        this.vertx = vertx;
        return this;
    }

    public OriginVertxContext withHttpServer(HttpServer httpServer) {
        this.server = httpServer;
        return this;
    }

    public OriginVertxContext withRouter(Router router) {
        this.router = router;
        return this;
    }


}
