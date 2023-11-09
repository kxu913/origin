package com.origin.starter.web;

import com.origin.starter.web.core.HttpStarter;
import com.origin.starter.web.core.OriginWebBeanFactory;
import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.core.OriginRouterFactory;
import com.origin.starter.web.domain.OriginVertxContext;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;


import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OriginWebApplication {
    private static final ThreadLocal<OriginVertxContext> VERTX_CONTENT_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<OriginConfig> CONFIG_FACTORY_THREAD_LOCAL = new ThreadLocal<>();

    private static final ThreadLocal<OriginWebBeanFactory> ORIGIN_BEAN_FACTORY_THREAD_LOCAL = new ThreadLocal<>();

    public static void runAsSingle(Class<? extends Verticle> clazz) {
        VertxOptions options = new VertxOptions()
                .setMaxWorkerExecuteTime(30)
                .setWorkerPoolSize(40)
                .setMaxWorkerExecuteTimeUnit(TimeUnit.SECONDS)
                .setEventLoopPoolSize(8)
                .setMetricsOptions(
                        new MicrometerMetricsOptions()
                                .setPrometheusOptions(
                                        new VertxPrometheusOptions()
                                                .setEnabled(true))
                                .setEnabled(true));
        try {
            Vertx vertx = Vertx.vertx(options);
            log.info("* start initializing a single origin web instance...");
            init(vertx, clazz);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static void runAsCluster(Class<? extends Verticle> clazz) {
        ClusterManager mgr = new ZookeeperClusterManager();
        VertxOptions options = new VertxOptions()
                .setClusterManager(mgr)
                .setMetricsOptions(
                        new MicrometerMetricsOptions()
                                .setPrometheusOptions(
                                        new VertxPrometheusOptions()
                                                .setEnabled(true))
                                .setEnabled(true));
        Vertx.clusteredVertx(options).onComplete(ar -> {
            if (ar.succeeded()) {
                try {
                    Vertx vertx = ar.result();
                    log.info("* start initializing a cluster origin web instance...");
                    init(vertx, clazz);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    private static void init(Vertx vertx, Class<? extends Verticle> clazz) {

        OriginVertxContext originVertxContext = new OriginVertxContext().fromVertx(vertx);
        OriginConfig originConfig = new OriginConfig().fromVertx(vertx);
        OriginWebBeanFactory beanFactory = new OriginWebBeanFactory(originVertxContext);
        log.info("** prepared context and config.");

        Future<Void> starterFuture = new HttpStarter(originVertxContext, originConfig).startHttpServer();
        log.info("** prepared start http server.");
        Future<Map<String, JsonObject>> loadBeanFutures = beanFactory.loadBeanConfig();
        log.info("** prepared load bean configuration.");
        Future<String> deployFuture = vertx.deployVerticle(clazz, new DeploymentOptions());
        log.info("** prepared start http server.");
        Future.all(starterFuture, loadBeanFutures, deployFuture).onComplete(cf -> {
            if (cf.succeeded()) {
                List<Object> results = cf.result().list();
                log.info("*** server started, ready to accept  requests.");
                VERTX_CONTENT_THREAD_LOCAL.set(originVertxContext);
                CONFIG_FACTORY_THREAD_LOCAL.set(originConfig);
                ORIGIN_BEAN_FACTORY_THREAD_LOCAL.set(beanFactory);
                log.info("*** beanfactory injected, detail {}", results.get(1));
                new OriginRouterFactory(originVertxContext, originConfig).register();
                log.info("*** router injected");
                log.info("* {}'s server start finished.", clazz.getName());
            } else {
                throw new RuntimeException(cf.cause());
            }
        }).onFailure(err -> {
            throw new RuntimeException(err.getCause());
        });
    }


    public static OriginConfig getConfig() {
        return CONFIG_FACTORY_THREAD_LOCAL.get();
    }

    public static OriginVertxContext getContext() {
        return VERTX_CONTENT_THREAD_LOCAL.get();
    }

    public static OriginWebBeanFactory getBeanFactory() {
        return ORIGIN_BEAN_FACTORY_THREAD_LOCAL.get();
    }


}
