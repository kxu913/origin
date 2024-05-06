package com.origin.starter.web.router;

import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.spi.DBData;
import com.origin.framework.spi.OriginRouter;
import com.origin.starter.common.data.DBDataProcess;
import com.origin.starter.web.OriginWebApplication;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

@Slf4j
public class DBDataRouter implements OriginRouter {
    @Override
    public void router(OriginWebVertxContext originWebVertxContext, OriginConfig originConfig) {
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
            SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient(dbName);
            DBDataProcess.process(originConfig, sqlClient, dbData);
        });
    }


}
