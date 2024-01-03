package com.origin.framework.core.factory;


import com.origin.framework.core.bean.OriginVertxContext;
import com.origin.framework.core.exception.InvalidBeanException;
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
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class OriginBeanFactory<T extends OriginVertxContext> {
    private final T vc;

    private final Map<String, JsonObject> beanConfigs;

    public OriginBeanFactory(T vc) {
        this.vc = vc;
        beanConfigs = new HashMap<>();
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
                    });
        });

    }

    private void splitConfig(JsonObject config) {
        config.getMap().forEach((k, v) -> {
            if (v instanceof Map<?, ?>) {
                beanConfigs.put(k, new JsonObject((Map<String, Object>) v));
            }
        });
    }

    public SqlClient getSqlClient() {
        return getSqlClient("db");
    }

    public SqlClient getSqlClient(String name) {
        JsonObject dbConfig = beanConfigs.get(name);
        if (dbConfig == null) {
            throw new InvalidBeanException(name + " config is null, set " + name + " in conf/config.json.");
        }
        return PgPool.client(
                vc.getVertx(), new PgConnectOptions(dbConfig), new PoolOptions(dbConfig.getJsonObject("pool")));
    }

    public Redis getRedisClient() {
        return getRedisClient("redis");
    }

    public Redis getRedisClient(String name) {
        JsonObject redisConfig = beanConfigs.get(name);
        if (redisConfig == null) {
            throw new InvalidBeanException("redis config is null, set redis in conf/config.json.");
        }
        return Redis.createClient(vc.getVertx(), new RedisOptions(redisConfig));
    }

    public RestClient getESRestClient() {
        return this.getESRestClient("es");

    }

    public RestClient getESRestClient(String name) {
        JsonObject esConfig = beanConfigs.get(name);
        if (esConfig == null) {
            throw new InvalidBeanException("elastic search config is null, set es in conf/config.json.");
        }
        return RestClient.builder(
                        new HttpHost(
                                esConfig.getString("host"),
                                esConfig.getInteger("port"),
                                esConfig.getString("schema")))
                .setDefaultHeaders(
                        new Header[]{
                                new BasicHeader("Content-type", esConfig.getString("data-type"))
                        })
                .setHttpClientConfigCallback(httpAsyncClientBuilder ->
                        httpAsyncClientBuilder
                                .addInterceptorLast((HttpResponseInterceptor) (response, context) ->
                                        response.setHeader("X-Elastic-Product", esConfig.getString("product-id"))
                                )
                                .setMaxConnPerRoute(100))
                .build();
    }

}
