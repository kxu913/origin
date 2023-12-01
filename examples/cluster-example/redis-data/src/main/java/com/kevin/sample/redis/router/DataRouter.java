package com.kevin.sample.redis.router;

import com.origin.starter.web.OriginWebApplication;
import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.domain.OriginVertxContext;
import com.origin.starter.web.handler.AsyncResultHandler;
import com.origin.starter.web.spi.OriginRouter;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class DataRouter implements OriginRouter {

    @Override
    public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {
        Future<RedisConnection> redisConnectionFuture = OriginWebApplication.getBeanFactory().getRedisClient().connect();
        AsyncResultHandler.handleFuture(redisConnectionFuture, redisConnection -> {
            originConfig.getEventBus().consumer("data")
                    .handler(ar -> {
                        setDataV2(originVertxContext, redisConnection, "data", Long.parseLong(ar.body().toString()), () -> {
                        });
                    }).endHandler(end -> {
                        redisConnection.close();
                    });
        });


        originVertxContext.getRouter().get("/data/:key")
                .handler(ctx -> {
                    String key = ctx.pathParam("key");
                    getData(originVertxContext, key, ar -> {
                        ctx.response().setChunked(true);
                        ctx.json(new JsonObject().put("data", ar.stream().map(Response::toLong).toList()));
                        return null;
                    });
                });

    }


    private void setDataV2(OriginVertxContext originVertxContext, RedisConnection connection, String key, long o, Runnable handleData) {


        Request request = Request.cmd(Command.SADD).arg(key).arg(o);
        connection.send(request);
        handleData.run();


    }


    private void getData(OriginVertxContext originVertxContext, String key, Function<Response, Void> handleData) {
        OriginWebApplication.getBeanFactory().getRedisClient()
                .connect().onComplete(ar -> {
                    if (ar.succeeded()) {
                        RedisConnection connection = ar.result();
                        connection.send(Request.cmd(Command.SMEMBERS).arg(key))
                                .onComplete(message -> {
                                    if (message.succeeded()) {
                                        Response response = message.result();
                                        handleData.apply(response);
                                    }

                                }).onFailure(Throwable::printStackTrace);
                        connection.close();
                    }
                });
    }
}
