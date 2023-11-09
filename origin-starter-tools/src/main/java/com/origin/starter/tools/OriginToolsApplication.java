package com.origin.starter.tools;

import com.origin.starter.tools.core.ToolsLoader;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OriginToolsApplication {

    public static void run(Class<? extends Verticle> clazz) {
        VertxOptions options = new VertxOptions()
                .setMetricsOptions(
                        new MicrometerMetricsOptions()
                                .setPrometheusOptions(
                                        new VertxPrometheusOptions()
                                                .setEnabled(true))
                                .setEnabled(true));
        try {
            Vertx vertx = Vertx.vertx(options);
            vertx.deployVerticle(clazz.getDeclaredConstructor().newInstance()).onComplete(ar -> {
                if (ar.succeeded()) {
                    log.info("{} had been deployed, and deploymentId is {}.", clazz.getName(), ar.result());
                    initTool(vertx);
                }

            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static void initTool(Vertx vertx) {
        ToolsLoader loader = new ToolsLoader(vertx);
        loader.load();
    }


}
