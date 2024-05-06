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
        String queue;
        String taskName = dbData.taskName();
        if (taskName != null) {
            JsonObject config = originConfig.getAppConfig().getJsonObject(dbData.collectionConfig());
            queue = config.getJsonObject(taskName).getString("queue");
        } else {
            queue = dbData.queueName();
        }
        String insertSql = dbData.insertSQL();
        String rollbackSql = dbData.rollbackSQL();
        int batchSize = dbData.batchSize();
        log.debug("start consume message from {}, prepare insert {} records to database.", queue, batchSize);
        List<DBData> dbDataList = new ArrayList<>();
        originConfig.getEventBus().consumer(queue, message -> {
            String action = message.headers().get("action");
            String sql = action != null && action.equalsIgnoreCase("delete") ? rollbackSql : insertSql;
            String end = message.body().toString();
            if (end.equals("end")) {
                batchInsert(sqlClient, sql, dbDataList, batchSize);
                log.info("batch process reached end.");
                return;
            }
            try {
                DBData innerData = new JsonObject(message.body().toString()).mapTo(dbData.getClass());
                dbDataList.add(innerData);
                if (dbDataList.size() == batchSize) {
                    batchInsert(sqlClient, sql, dbDataList, batchSize);
                    dbDataList.clear();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }).endHandler(v -> {
            sqlClient.close();
        });
    }

    private static void batchInsert(SqlClient sqlClient, String sql, List<DBData> dbDataList, int batchSize) {
        List<DBData> dbDataListCopy = new ArrayList<>(dbDataList);

        if (dbDataListCopy.isEmpty()) {
            return;
        }

        List<Tuple> batch = dbDataListCopy.stream().map(DBData::toTuple).toList();
        Future<RowSet<Row>> future = sqlClient.preparedQuery(sql)
                .executeBatch(batch);
        future.onSuccess(ar -> {
                    log.info("batch insert {} records to database.", dbDataListCopy.size());
                    if (dbDataListCopy.size() < batchSize) {
                        log.info("reach end, tha last batch size is {}.", dbDataListCopy.size());
                    }

                })
                .onFailure(err -> {
                    log.error(err.getMessage(), err);
                });
    }
}
