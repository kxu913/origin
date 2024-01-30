package com.kevin.sample.vertx.blog.router;

import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.spi.OriginRouter;
import com.origin.starter.web.OriginWebApplication;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.Request;

public class RedisRouter implements OriginRouter {
    @Override
    public void router(OriginWebVertxContext vc, OriginConfig cf) {
        vc.getRouter().post("/redis/set")
                .handler(ctx -> {
                    ctx.request().body().onSuccess(body -> {
                        String key = body.toString();
                        OriginWebApplication.getRedisShardingFactory().execute(Request.cmd(Command.SADD).arg("sample").arg(key), key)
                                .onSuccess(res -> {
                                    ctx.response().end(res.toString());
                                })
                                .onFailure(ctx::fail);
                    });

                });
        vc.getRouter().post("/redis/hash")
                .handler(ctx -> {
                    ctx.request().body().onSuccess(body -> {
                        JsonObject jsonObject = body.toJsonObject();
                        String value = jsonObject.getString("value");
                        try {
                            JsonObject v = jsonObject.getJsonObject("value");
                            value = v.toString();
                        } catch (Exception ignore) {

                        }
                        OriginWebApplication.getRedisShardingFactory().execute(Request.cmd(Command.HSET)
                                        .arg("sample-hash")
                                        .arg(jsonObject.getString("key"))
                                        .arg(value), jsonObject.getString("key"))
                                .onSuccess(res -> {
                                    ctx.response().end(res.toString());
                                })
                                .onFailure(ctx::fail);
                    });

                });
        vc.getRouter().get("/redis/:index/:key")
                .handler(ctx -> {
                    var index = ctx.pathParam("index");
                    var key = ctx.pathParam("key");
                    OriginWebApplication.getRedisShardingFactory().execute(Request.cmd(Command.HGET).arg(index).arg(key), key)
                            .onSuccess(res -> {
                                ctx.response().end(res.toString());
                            })
                            .onFailure(ctx::fail);

                });

        vc.getRouter().get("/redis-set/:index")
                .handler(ctx -> {
                    var index = ctx.pathParam("index");
                    OriginWebApplication.getRedisShardingFactory().retrieveData("set", index, null)
                            .onSuccess(ctx::json)
                            .onFailure(ctx::fail);

                });
        vc.getRouter().get("/redis-hash/:index")
                .handler(ctx -> {
                    var index = ctx.pathParam("index");
                    OriginWebApplication.getRedisShardingFactory().retrieveData("hash", index, json -> json.getString("name") == null)
                            .onSuccess(ctx::json)
                            .onFailure(ctx::fail);

                });
    }
}
