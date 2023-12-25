package com.origin.starter.common.data;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.spi.ESData;
import com.origin.framework.spi.RedisData;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.Request;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RedisHashDataProcess {

    public static void process(OriginConfig originConfig, Future<RedisConnection> redisConnectionFuture, RedisData redisData) {
        String queue = redisData.queueName();
        String hashKey = redisData.redisHashKey();
        String objectKey = redisData.key();
        log.info("start consume message from {}, store data in redis hashkey {} and object key is {}.", queue, hashKey, objectKey);
        redisConnectionFuture
                .onSuccess(redisConnection -> {
                    originConfig.getEventBus().consumer(queue, message -> {
                        String msg = message.body().toString();
                        if (msg.equals("end")) {
                            return;
                        }
                        JsonObject jsonObject = new JsonObject(message.body().toString());
                        String id = jsonObject.getString(objectKey);
                        if (!id.isEmpty()) {
                            Request request = Request.cmd(Command.HSET).arg(hashKey).arg(id).arg(message.body().toString());
                            redisConnection.send(request, rtn -> {
                                if (!rtn.succeeded()) {
                                    log.error(rtn.cause().getMessage(), rtn.cause());
                                }
                            });
                        }
                    });
                })
                .onFailure(err -> log.error(err.getMessage(), err));

    }
}
