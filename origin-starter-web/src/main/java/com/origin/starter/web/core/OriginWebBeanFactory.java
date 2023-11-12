package com.origin.starter.web.core;


import com.origin.starter.web.domain.OriginVertxContext;
import com.origin.starter.web.exception.InvalidBeanException;
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
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class OriginWebBeanFactory {
    private final OriginVertxContext vc;

    private final Map<String, JsonObject> beanConfigs;

    public OriginWebBeanFactory(OriginVertxContext vc) {
        this.vc = vc;
        beanConfigs = new HashMap<>();
        beanConfigs.put("db", null);
        beanConfigs.put("redis", null);
        beanConfigs.put("es", null);
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
        if (dbConfig == null) {
            throw new InvalidBeanException("db config is null, set db in conf/config.json.");
        }
        return PgPool.client(
                vc.getVertx(), new PgConnectOptions(dbConfig), new PoolOptions(dbConfig.getJsonObject("pool")));
    }

    public Redis getRedisClient() {
        JsonObject redisConfig = beanConfigs.get("redis");
        if (redisConfig == null) {
            throw new InvalidBeanException("redis config is null, set redis in conf/config.json.");
        }
        return Redis.createClient(vc.getVertx(), new RedisOptions(redisConfig));
    }

    public RestClient getESRestClient() {
        JsonObject esConfig = beanConfigs.get("es");
        return RestClient.builder(new HttpHost(esConfig.getString("host"), esConfig.getInteger("port"), esConfig.getString("schema")))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Content-type", esConfig.getString("data-type"))
                }).build();
    }

}
