package com.origin.starter.app.task;

import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginVertxContext;
import com.origin.framework.spi.OriginTask;
import com.origin.framework.spi.RedisData;
import com.origin.starter.app.OriginAppApplication;
import com.origin.starter.common.data.RedisSetDataProcess;
import io.vertx.core.Future;
import io.vertx.redis.client.RedisConnection;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

@Slf4j
public class RedisSetDataTask implements OriginTask {
    @Override
    public void run(OriginVertxContext originVertxContext, OriginConfig originConfig) {
        ServiceLoader<RedisData> loader = ServiceLoader.load(RedisData.class);
        loader.forEach(redisData -> {
            Future<RedisConnection> redisConnectionFuture = OriginAppApplication.getBeanFactory().getRedisClient().connect();
            RedisSetDataProcess.process(originConfig, redisConnectionFuture, redisData);
        });
    }

}
