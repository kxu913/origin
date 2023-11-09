package com.origin.starter.app.core;


import com.origin.starter.app.domain.OriginAppVertxContext;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.Future;

import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class OriginAppBeanFactory {
    private final OriginAppVertxContext vc;

    private final Map<String, JsonObject> beanConfigs;

    public OriginAppBeanFactory(OriginAppVertxContext vc) {
        this.vc = vc;
        beanConfigs = new HashMap<>();
        beanConfigs.put("db", null);
        beanConfigs.put("redis", null);
    }

    public Future<Map<String, JsonObject>> loadBeanConfig() {
        return Future.future(ar -> {
            ConfigRetriever.create(vc.getVertx())
                    .getConfig()
                    .onComplete(arJson -> {
                        if (arJson.succeeded()) {
                            JsonObject config = arJson.result();
                            splitConfig(config);
                            ar.complete(beanConfigs);

                        } else {
                            log.error("get config error.", arJson.cause());
                            ar.fail(arJson.cause());
                        }
                    }).onFailure(err -> {
                        log.error("something went wrong.", err);
                        ar.fail(err);
                    });
        });

    }

    private void splitConfig(JsonObject config) {
        beanConfigs.forEach((k, v) -> {
            if (config.containsKey(k)) {
                JsonObject childConfig = config.getJsonObject(k);
                beanConfigs.put(k, childConfig);
            }
        });
    }

    public SqlClient getSqlClient() {
        JsonObject dbConfig = beanConfigs.get("db");
        return PgPool.client(
                vc.getVertx(), new PgConnectOptions(dbConfig), new PoolOptions(dbConfig.getJsonObject("pool")));
    }

    public Redis getRedisClient() {
        JsonObject redisConfig = beanConfigs.get("redis");
        return Redis.createClient(vc.getVertx(), new RedisOptions(redisConfig));
    }


}
