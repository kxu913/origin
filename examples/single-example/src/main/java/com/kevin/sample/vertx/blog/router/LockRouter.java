package com.kevin.sample.vertx.blog.router;


import com.origin.starter.web.OriginWebApplication;
import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.domain.OriginVertxContext;
import com.origin.starter.web.handler.AsyncResultHandler;
import com.origin.starter.web.handler.LockHandler;
import com.origin.starter.web.spi.OriginRouter;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public class LockRouter implements OriginRouter {
    @Override
    public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {

        // when you use jmeter (200 thread) test the case, due to the api use lock, you'll get the correct result as expected.
        originVertxContext.getRouter().get("/lock")
                .handler(ctx -> {
                    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
                    LockHandler.runWithLock(originVertxContext, ctx, lock -> {
                        AsyncResultHandler.handleFuture(
                                sqlClient.preparedQuery("select * from demo limit 1").execute(),
                                ctx,
                                rowSet -> {
                                    JsonObject total = new JsonObject();
                                    for (Row row : rowSet) {
                                        total = row.toJson();
                                    }
                                    if (total.getInteger("total") > 0) {
                                        int remain = total.getInteger("total") - 50;
                                        sqlClient.preparedQuery("update demo set total=$1")
                                                .execute(Tuple.of(remain))
                                                .onComplete(e -> {
                                                    sqlClient.close();
                                                    lock.release();
                                                    ctx.end("update total to " + remain);
                                                });
                                    } else {

                                        sqlClient.close();
                                        lock.release();
                                        ctx.end("no need to update");
                                    }

                                });
                    });

                });

        // if you use jmeter test (200 threads) the case, you will get some unexpected issues, such as the total incorrect at last, because each thread use its own initial total number.
        originVertxContext.getRouter().get("/nolock")
                .handler(ctx -> {
                    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
                    AsyncResultHandler.handleFuture(
                            sqlClient.preparedQuery("select * from demo limit 1").execute(),
                            ctx,
                            rowSet -> {
                                JsonObject total = new JsonObject();
                                for (Row row : rowSet) {
                                    total = row.toJson();
                                }
                                if (total.getInteger("total") > 0) {
                                    int remain = total.getInteger("total") - 50;
                                    sqlClient.preparedQuery("update demo set total=$1")
                                            .execute(Tuple.of(remain))
                                            .onComplete(e -> {
                                                sqlClient.close();
                                                ctx.end("update total to " + remain);
                                            });
                                } else {
                                    sqlClient.close();
                                    ctx.end("no need to update");
                                }

                            });
                });


    }
}
