package com.origin.starter.common.data;

import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.spi.DBData;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DBDataProcess {

    public static void process(OriginConfig originConfig, SqlClient sqlClient, DBData dbData) {
        String queue = dbData.queueName();
        String sql = dbData.insertSQL();
        int batchSize = dbData.batchSize();
        log.debug("start consume message from {}, prepare insert {} records to database using sql {}.", queue, batchSize, sql);
        List<DBData> dbDataList = new ArrayList<>();
        originConfig.getEventBus().consumer(queue, message -> {
            String end = message.body().toString();
            if (end.equals("end")) {
                batchInsert(sqlClient, sql, dbDataList);
                log.info("reach end.");
                return;
            }
            try {
                DBData innerData = new JsonObject(message.body().toString()).mapTo(dbData.getClass());
                dbDataList.add(innerData);
                if (dbDataList.size() == batchSize) {
                    batchInsert(sqlClient, sql, dbDataList);
                    dbDataList.clear();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }).endHandler(v -> {
            sqlClient.close();
        });
    }

    private static void batchInsert(SqlClient sqlClient, String sql, List<DBData> dbDataList) {
        List<Tuple> batch = dbDataList.stream().map(DBData::toTuple).toList();
        Future<RowSet<Row>> future = sqlClient.preparedQuery(sql)
                .executeBatch(batch);
        future.onSuccess(ar -> {
                    if (log.isDebugEnabled()) {
                        log.debug("actually insert {} records to database.", dbDataList.size());
                    }

                })
                .onFailure(err -> {
                    log.error(err.getMessage(), err);
                });
    }
}
