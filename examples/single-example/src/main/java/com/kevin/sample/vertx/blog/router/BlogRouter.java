package com.kevin.sample.vertx.blog.router;


import com.origin.starter.web.OriginWebApplication;
import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.domain.OriginVertxContext;
import com.origin.starter.web.handler.AsyncResultHandler;
import com.origin.starter.web.spi.OriginRouter;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public class BlogRouter implements OriginRouter {
    @Override
    public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {

        // a simple example that handle ctx in handler method.
        originVertxContext.getRouter().get("/blog")
                .handler(ctx -> {
                    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
                    AsyncResultHandler.handleFuture(
                            sqlClient.preparedQuery("select * from blog limit 10").execute(),
                            ctx,
                            rowSet -> {
                                List<JsonObject> results = new ArrayList<>(rowSet.size());
                                rowSet.forEach(row -> results.add(row.toJson()));
                                ctx.json(results);
                                sqlClient.close();
                                return null;
                            });

                });
        // a simple example that return an object and handle it in high level method, suggested.
        originVertxContext.getRouter().get("/blog2")
                .handler(ctx -> {
                    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
                    AsyncResultHandler.handleFutureWithReturn(
                            sqlClient.preparedQuery("select * from blog limit 10").execute(),
                            ctx,
                            rowSet -> {
                                List<JsonObject> results = new ArrayList<>(rowSet.size());
                                rowSet.forEach(row -> results.add(row.toJson()));
                                sqlClient.close();
                                return results;
                            });

                });
        // a complex example that combine AsyncResultHandler.handleFutureWithReturn and AsyncResultHandler.handleAsyncResult in same method.
        originVertxContext.getRouter().get("/complex")
                .handler(ctx -> {
                    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
                    AsyncResultHandler.handleFutureWithReturn(
                            sqlClient.preparedQuery("select * from blog limit 10").execute(),
                            ctx,
                            rowSet -> {
                                List<JsonObject> results = new ArrayList<>(rowSet.size());
                                rowSet.forEach(row -> results.add(row.toJson()));
                                sqlClient.close();
                                Buffer buffer = Buffer.buffer();
                                results.stream().map(JsonObject::toBuffer).forEach(buffer::appendBuffer);
                                originVertxContext.getFs().writeFile(
                                        "t.txt",
                                        buffer,
                                        ar -> {
                                            AsyncResultHandler.handleAsyncResult(ar, ctx, v -> null);
                                        }
                                );
                                return results;
                            });

                });


    }
}
