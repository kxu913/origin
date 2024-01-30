package com.origin.starter.web;

import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.core.factory.OriginBeanFactory;
import com.origin.framework.redis.factory.RedisShardingFactory;
import com.origin.framework.spi.RedisShardingAlgorithm;
import com.origin.starter.web.core.HttpStarter;
import com.origin.starter.web.core.OriginRouterFactory;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OriginWebApplication {
    private static final ThreadLocal<OriginWebVertxContext> VERTX_CONTENT_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<OriginConfig> CONFIG_FACTORY_THREAD_LOCAL = new ThreadLocal<>();

    private static final ThreadLocal<OriginBeanFactory<OriginWebVertxContext>> ORIGIN_BEAN_FACTORY_THREAD_LOCAL = new ThreadLocal<>();

    private static final ThreadLocal<RedisShardingFactory<OriginWebVertxContext>> REDIS_SHARDING_FACTORY_THREAD_LOCAL = new ThreadLocal<>();

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

        OriginWebVertxContext originVertxContext = new OriginWebVertxContext().fromVertx(vertx);
        OriginConfig originConfig = new OriginConfig().fromVertx(vertx);
        OriginBeanFactory<OriginWebVertxContext> beanFactory = new OriginBeanFactory<>(originVertxContext);
        List<Future<?>> initFuture = new ArrayList<>();
        log.info("** prepared context and config.");
        Future<JsonObject> configFuture = originConfig.getRetriever().getConfig();
        initFuture.add(configFuture);
        log.info("** prepared load app config.");

        Future<Void> starterFuture = new HttpStarter(originVertxContext, originConfig).startHttpServer();
        initFuture.add(starterFuture);
        log.info("** prepared start http server.");
        Future<Map<String, JsonObject>> loadBeanFutures = beanFactory.loadBeanConfig();
        initFuture.add(loadBeanFutures);
        log.info("** prepared load bean configuration.");
        conditionBeans(originVertxContext, initFuture);
        Future<String> deployFuture = vertx.deployVerticle(clazz, new DeploymentOptions());
        initFuture.add(deployFuture);
        log.info("** prepared start http server.");

        Future.all(initFuture).onComplete(cf -> {
            if (cf.succeeded()) {
                List<Object> results = cf.result().list();
                JsonObject appConfig = (JsonObject) results.get(0);
                originConfig.setAppConfig(appConfig);
                log.info("*** app config loaded, app config is {}.", appConfig);
                CONFIG_FACTORY_THREAD_LOCAL.set(originConfig);

                log.info("*** server started, ready to accept  requests.");
                VERTX_CONTENT_THREAD_LOCAL.set(originVertxContext);
                ORIGIN_BEAN_FACTORY_THREAD_LOCAL.set(beanFactory);
                log.info("*** beanfactory injected, detail {}", results.get(2));
                if (initFuture.size() == 5) {
                    REDIS_SHARDING_FACTORY_THREAD_LOCAL.set((RedisShardingFactory<OriginWebVertxContext>) results.get(3));
                    log.info("*** additional bean redis sharding factory injected, detail {}", results.get(3));
                }
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

    private static void conditionBeans(OriginWebVertxContext originVertxContext, List<Future<?>> initFuture) {
        ServiceLoader<RedisShardingAlgorithm> loader = ServiceLoader.load(RedisShardingAlgorithm.class);
        if (loader.findFirst().isPresent()) {
            var pattern = loader.iterator().next();
            var factory = new RedisShardingFactory<>(originVertxContext, pattern);
            initFuture.add(factory.loadRedisConfig());
            log.info("** prepared load redis sharding factory.");

        }
    }


    public static OriginConfig getConfig() {
        return CONFIG_FACTORY_THREAD_LOCAL.get();
    }

    public static OriginWebVertxContext getContext() {
        return VERTX_CONTENT_THREAD_LOCAL.get();
    }

    public static OriginBeanFactory<OriginWebVertxContext> getBeanFactory() {
        return ORIGIN_BEAN_FACTORY_THREAD_LOCAL.get();
    }

    public static RedisShardingFactory<OriginWebVertxContext> getRedisShardingFactory() {
        return REDIS_SHARDING_FACTORY_THREAD_LOCAL.get();
    }


}
