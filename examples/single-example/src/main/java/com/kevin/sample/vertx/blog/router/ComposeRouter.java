package com.kevin.sample.vertx.blog.router;

import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.spi.OriginRouter;
import com.origin.starter.web.OriginWebApplication;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComposeRouter implements OriginRouter {
    @Override
    public void router(OriginWebVertxContext originWebVertxContext, OriginConfig originConfig) {
        originWebVertxContext.getRouter().route("/compose")
                .handler(ctx -> {
                    SqlClient remoteSqlClient = OriginWebApplication.getBeanFactory().getSqlClient("remote-db");
                    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
                    JsonObject customJsonObject = originConfig.getAppConfig().getJsonObject("custom-config");
                    Future<RowSet<Row>> blogFuture = sqlClient.preparedQuery("select * from blog limit 1").execute();
                    Future<RowSet<Row>> blog2Future = remoteSqlClient.preparedQuery("select * from blog limit 1").execute();
                    Future.all(blogFuture, blog2Future)
                            .onSuccess(ar -> {
                                List<RowSet<Row>> list = ar.list();
                                Map<String, Object> result = new HashMap<>();
                                list.forEach(rowset -> {
                                    rowset.forEach(row -> {
                                        JsonObject blog = row.toJson();
                                        result.putAll(blog.getMap());
                                    });
                                });
                                result.putAll(customJsonObject.getMap());
                                ctx.json(result);
                                sqlClient.close();
                                remoteSqlClient.close();
                            })
                            .onFailure(err -> ctx.fail(500, err));
                });

    }
}
