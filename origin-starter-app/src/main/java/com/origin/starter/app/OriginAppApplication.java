package com.origin.starter.app;


import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginVertxContext;
import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.core.factory.OriginBeanFactory;
import com.origin.framework.redis.factory.RedisShardingFactory;
import com.origin.framework.spi.RedisShardingAlgorithm;
import com.origin.starter.app.core.OriginTaskFactory;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;


@Slf4j
public class OriginAppApplication {
    private static final ThreadLocal<OriginVertxContext> VERTX_CONTENT_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<OriginConfig> CONFIG_FACTORY_THREAD_LOCAL = new ThreadLocal<>();

    private static final ThreadLocal<OriginBeanFactory<OriginVertxContext>> APP_BEAN_FACTORY_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<RedisShardingFactory<OriginWebVertxContext>> REDIS_SHARDING_FACTORY_THREAD_LOCAL = new ThreadLocal<>();


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
            OriginVertxContext originVertxContext = new OriginVertxContext().fromVertx(vertx);
            OriginConfig originConfig = new OriginConfig().fromVertx(vertx);
            List<Future<?>> initFuture = new ArrayList<>();
            log.info("** prepared context and config.");
            OriginBeanFactory<OriginVertxContext> appBeanFactory = new OriginBeanFactory<>(originVertxContext);
            Future<JsonObject> configFuture = originConfig.getRetriever().getConfig();
            initFuture.add(configFuture);
            log.info("** prepared load app configuration.");
            Future<Map<String, JsonObject>> appBeanFactoryFuture = appBeanFactory.loadBeanConfig();
            initFuture.add(appBeanFactoryFuture);
            conditionBeans(originVertxContext, initFuture);
            log.info("** prepared load bean configuration.");
            Future<String> deployFuture = vertx.deployVerticle(clazz.getDeclaredConstructor().newInstance());
            initFuture.add(deployFuture);
            Future.all(initFuture).onComplete(ar -> {
                if (ar.succeeded()) {
                    List<Object> results = ar.result().list();
                    JsonObject appConfig = (JsonObject) results.get(0);
                    originConfig.setAppConfig(appConfig);
                    VERTX_CONTENT_THREAD_LOCAL.set(originVertxContext);
                    CONFIG_FACTORY_THREAD_LOCAL.set(originConfig);
                    log.info("*** context, config injected.");
                    APP_BEAN_FACTORY_THREAD_LOCAL.set(appBeanFactory);
                    log.info("*** beanFactory injected, detail {}", results.get(1));
                    if (initFuture.size() == 4) {
                        REDIS_SHARDING_FACTORY_THREAD_LOCAL.set((RedisShardingFactory<OriginWebVertxContext>) results.get(2));
                        log.info("*** additional bean redis sharding factory injected, detail {}", results.get(2));
                    }
                    new OriginTaskFactory(originVertxContext, originConfig).register();
                    log.info("*** taskBean injected.");
                    log.info("* {}'s app start finished.", clazz.getName());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static void conditionBeans(OriginVertxContext originVertxContext, List<Future<?>> initFuture) {
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

    public static OriginVertxContext getContext() {
        return VERTX_CONTENT_THREAD_LOCAL.get();
    }

    public static OriginBeanFactory<OriginVertxContext> getBeanFactory() {
        return APP_BEAN_FACTORY_THREAD_LOCAL.get();
    }

    public static RedisShardingFactory<OriginWebVertxContext> getRedisShardingFactory() {
        return REDIS_SHARDING_FACTORY_THREAD_LOCAL.get();
    }

}
