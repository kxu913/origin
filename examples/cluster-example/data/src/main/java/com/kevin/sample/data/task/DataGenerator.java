package com.kevin.sample.data.task;

import com.origin.starter.app.OriginAppApplication;
import com.origin.starter.app.domain.OriginAppConfig;
import com.origin.starter.app.domain.OriginAppVertxContext;
import com.origin.starter.app.spi.OriginTask;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class DataGenerator implements OriginTask {
    private final Random random = new Random();

    private long generatePhoneNum() {
        Random random = new Random();
        return 13L * 1000000000 + 100000000 + random.nextInt(999999999);

    }

    @Override
    public void run(OriginAppVertxContext originAppVertxContext, OriginAppConfig originAppConfig) {
//        SqlClient sqlClient = OriginAppApplication.getBeanFactory().getSqlClient();
//        sqlClient.preparedQuery("select * from blog limit 1").execute().onComplete(ar -> {
//            if (ar.succeeded()) {
//                RowSet<Row> result = ar.result();
//                result.forEach(row -> log.info("{}", row.toJson()));
//            }
//            sqlClient.close();
//        });

        /**
         * simplify example
         */
        originAppVertxContext.getVertx().setPeriodic(1000, t -> {
            long x = generatePhoneNum();
            System.out.println(x);
            originAppConfig.getEventBus().publish("data", x);
        });
    }
}
