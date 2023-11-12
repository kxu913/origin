package com.kevin.sample.vertx.blog.router;


import com.origin.starter.web.OriginWebApplication;
import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.domain.OriginVertxContext;
import com.origin.starter.web.spi.OriginRouter;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;

import java.util.ArrayList;
import java.util.List;


public class BlogRouter implements OriginRouter {
    @Override
    public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {

        originVertxContext.getRouter().get("/blog")
                .handler(ctx -> {
                    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
                    sqlClient.preparedQuery("select * from blog limit 10").execute()
                            .onComplete(ar -> {
                                if (ar.succeeded()) {
                                    RowSet<Row> rowSet = ar.result();
                                    List<JsonObject> results = new ArrayList<>(rowSet.size());
                                    rowSet.forEach(row -> results.add(row.toJson()));
                                    ctx.json(results);
                                } else {
                                    ctx.fail(500, ar.cause());
                                }
                                sqlClient.close();
                            })
                            .onFailure(err -> ctx.fail(500, err));
                });

    }
}
