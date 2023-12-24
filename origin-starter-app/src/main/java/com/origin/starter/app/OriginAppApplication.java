package com.origin.starter.app;


import com.origin.framework.core.bean.OriginVertxContext;
import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.factory.OriginBeanFactory;
import com.origin.starter.app.core.OriginTaskFactory;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;


@Slf4j
public class OriginAppApplication {
    private static final ThreadLocal<OriginVertxContext> VERTX_CONTENT_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<OriginConfig> CONFIG_FACTORY_THREAD_LOCAL = new ThreadLocal<>();

    private static final ThreadLocal<OriginBeanFactory<OriginVertxContext>> APP_BEAN_FACTORY_THREAD_LOCAL = new ThreadLocal<>();


    public static void runAsSingle(Class<? extends Verticle> clazz) {

        VertxOptions options = new VertxOptions();
        try {
            Vertx vertx = Vertx.vertx(options);
            log.info("* start initializing a single origin app instance...");
            init(vertx, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static void runAsCluster(Class<? extends Verticle> clazz) {
        ClusterManager mgr = new ZookeeperClusterManager();
        VertxOptions options = new VertxOptions()
                .setClusterManager(mgr);
        Vertx.clusteredVertx(options).onComplete(ar -> {
            if (ar.succeeded()) {
                try {
                    Vertx vertx = ar.result();
                    log.info("* start initializing a cluster origin app instance...");
                    init(vertx, clazz);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    private static void init(Vertx vertx, Class<? extends Verticle> clazz) {
        try {
            OriginVertxContext originVertxContent = new OriginVertxContext().fromVertx(vertx);
            OriginConfig originConfig = new OriginConfig().fromVertx(vertx);
            log.info("** prepared context and config.");
            OriginBeanFactory<OriginVertxContext> appBeanFactory = new OriginBeanFactory<>(originVertxContent);
            Future<JsonObject> configFuture = originConfig.getRetriever().getConfig();
            log.info("** prepared load app configuration.");
            Future<Map<String, JsonObject>> appBeanFactoryFuture = appBeanFactory.loadBeanConfig();
            log.info("** prepared load bean configuration.");
            Future<String> deployFuture = vertx.deployVerticle(clazz.getDeclaredConstructor().newInstance());
            Future.all(configFuture, appBeanFactoryFuture, deployFuture).onComplete(ar -> {
                if (ar.succeeded()) {
                    List<Object> results = ar.result().list();
                    JsonObject appConfig = (JsonObject) results.get(0);
                    originConfig.setAppConfig(appConfig);
                    VERTX_CONTENT_THREAD_LOCAL.set(originVertxContent);
                    CONFIG_FACTORY_THREAD_LOCAL.set(originConfig);
                    log.info("*** context, config injected.");
                    APP_BEAN_FACTORY_THREAD_LOCAL.set(appBeanFactory);
                    log.info("*** beanFactory injected, detail {}", results.get(1));
                    new OriginTaskFactory(originVertxContent, originConfig).register();
                    log.info("*** taskBean injected.");
                    log.info("* {}'s app start finished.", clazz.getName());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public static OriginConfig getConfig() {
        return CONFIG_FACTORY_THREAD_LOCAL.get();
    }

    public static OriginVertxContext getContext() {
        return VERTX_CONTENT_THREAD_LOCAL.get();
    }

    public static OriginBeanFactory<OriginVertxContext> getBeanFactory() {
        return APP_BEAN_FACTORY_THREAD_LOCAL.get();
    }


}
