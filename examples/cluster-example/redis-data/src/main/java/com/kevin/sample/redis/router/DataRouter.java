package com.kevin.sample.redis.router;

import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.domain.OriginVertxContext;
import com.origin.starter.web.spi.OriginRouter;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class DataRouter implements OriginRouter {

    @Override
    public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {
        originConfig.getEventBus().consumer("data")
                .handler(ar -> {
                    setDataV2(originVertxContext, "data", Long.parseLong(ar.body().toString()), v -> null);
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


    private void setDataV2(OriginVertxContext originVertxContext, String key, long o, Function<Void, Void> handleData) {

        Redis.createClient(originVertxContext.getVertx(), new RedisOptions().setMaxWaitingHandlers(10000))
                .connect()
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        RedisConnection connection = ar.result();
                        Request request = Request.cmd(Command.SADD).arg(key).arg(o);
                        connection.send(request);
                        handleData.apply(null);
                        connection.close();
                    }


                });


    }


    private void getData(OriginVertxContext originVertxContext, String key, Function<Response, Void> handleData) {
        Redis.createClient(originVertxContext.getVertx())
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
