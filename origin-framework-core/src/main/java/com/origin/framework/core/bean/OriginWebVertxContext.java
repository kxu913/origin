package com.origin.framework.core.bean;

import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.web.Router;
import io.vertx.micrometer.PrometheusScrapingHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Data
@Slf4j
public class OriginWebVertxContext extends OriginVertxContext {

    private HttpServer server;
    private Router router;


    public OriginWebVertxContext fromVertx(Vertx vertx) {
        super.fromVertx(vertx);
        this.server = vertx.createHttpServer();
        this.router = Router.router(vertx);
        initRouter(vertx);
        return this;
    }

    private void initRouter(Vertx vertx) {
        this.router.route("/metrics").handler(PrometheusScrapingHandler.create());
        this.router.route("/health*").handler(HealthCheckHandler.create(vertx));
        this.router.route("/*").failureHandler(failureRoutingContext -> {
            log.error(failureRoutingContext.failure().getMessage(), failureRoutingContext.failure());
            int statusCode = failureRoutingContext.statusCode();
            HttpServerResponse response = failureRoutingContext.response();
            response.setStatusCode(statusCode).end("Something went wrong, error message is " + failureRoutingContext.failure().getMessage());

        });
        this.server.requestHandler(this.router);
    }


}
