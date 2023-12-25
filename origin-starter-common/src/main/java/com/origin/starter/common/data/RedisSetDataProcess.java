package com.origin.starter.common.data;

import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.spi.RedisData;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.Request;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisSetDataProcess {

    public static void process(OriginConfig originConfig, Future<RedisConnection> redisConnectionFuture, RedisData redisData) {
        String queue = redisData.queueName();
        String key = redisData.redisKey();
        String objectKey = redisData.key();
        log.info("start consume message from {}, store {} of data in redis setkey {}.", queue, objectKey, key);
        redisConnectionFuture
                .onSuccess(redisConnection -> {
                    originConfig.getEventBus().consumer(queue, message -> {
                        String msg = message.body().toString();
                        if (msg.equals("end")) {
                            return;
                        }
                        JsonObject jsonObject = new JsonObject(message.body().toString());
                        String uniqueKey = jsonObject.getString(objectKey);
                        if (!uniqueKey.isEmpty()) {
                            Request request = Request.cmd(Command.SADD).arg(key).arg(uniqueKey);
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
