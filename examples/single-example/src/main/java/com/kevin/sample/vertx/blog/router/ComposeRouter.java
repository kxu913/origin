package com.kevin.sample.vertx.blog.router;

import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.spi.OriginRouter;
import com.origin.starter.web.OriginWebApplication;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;

import java.util.ArrayList;
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
                    Future<RowSet<Row>> blogFuture = sqlClient.preparedQuery("select * from blog limit 10").execute();
                    Future<RowSet<Row>> blog2Future = remoteSqlClient.preparedQuery("select * from blog_detail limit 10").execute();
                    Future.all(blogFuture, blog2Future)
                            .onSuccess(ar -> {
                                // here is just a sample that combine columns from different datasource.
                                // actually you also can compose data from different datasource.
                                List<RowSet<Row>> list = ar.list();
                                RowSet<Row> blogRowSet = list.get(0);
                                RowIterator<Row> it = blogRowSet.iterator();
                                RowSet<Row> blog2RowSet = list.get(1);
                                RowIterator<Row> it2 = blog2RowSet.iterator();
                                List<Map<String, Object>> result = new ArrayList<>(blogRowSet.size());

                                for (int i = 0; i < blogRowSet.size(); i++) {
                                    Map<String, Object> row = new HashMap<>();
                                    row.putAll(it.next().toJson().getMap());
                                    row.putAll(it2.next().toJson().getMap());
                                    row.putAll(customJsonObject.getMap());
                                    result.add(row);
                                }

                                ctx.json(result);
                                sqlClient.close();
                                remoteSqlClient.close();
                            })
                            .onFailure(err -> ctx.fail(500, err));
                });

    }
}
