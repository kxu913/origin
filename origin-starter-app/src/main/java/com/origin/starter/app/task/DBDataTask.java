package com.origin.starter.app.task;

import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginVertxContext;
import com.origin.framework.spi.DBData;
import com.origin.framework.spi.OriginTask;
import com.origin.starter.app.OriginAppApplication;
import com.origin.starter.common.data.DBDataProcess;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

@Slf4j
public class DBDataTask implements OriginTask {
    @Override
    public void run(OriginVertxContext originWebVertxContext, OriginConfig originConfig) {
        ServiceLoader<DBData> loader = ServiceLoader.load(DBData.class);
        loader.forEach(dbData -> {
            String dbName;
            String taskName = dbData.taskName();
            if (taskName != null) {
                JsonObject config = originConfig.getAppConfig().getJsonObject(dbData.collectionConfig());
                dbName = config.getJsonObject(taskName).getString("db");
            } else {
                dbName = dbData.dbName();
            }
            SqlClient sqlClient = OriginAppApplication.getBeanFactory().getSqlClient(dbName);
            DBDataProcess.process(originConfig, sqlClient, dbData);
        });
    }
}
