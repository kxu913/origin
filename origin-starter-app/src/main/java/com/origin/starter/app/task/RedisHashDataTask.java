package com.origin.starter.app.task;

import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.spi.OriginRouter;
import com.origin.framework.spi.RedisData;
import com.origin.starter.app.OriginAppApplication;
import com.origin.starter.common.data.RedisHashDataProcess;
import io.vertx.core.Future;
import io.vertx.redis.client.RedisConnection;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

@Slf4j
public class RedisHashDataTask implements OriginRouter {
    @Override
    public void router(OriginWebVertxContext originWebVertxContext, OriginConfig originConfig) {
        ServiceLoader<RedisData> loader = ServiceLoader.load(RedisData.class);
        loader.forEach(redisData -> {
            Future<RedisConnection> redisConnectionFuture = OriginAppApplication.getBeanFactory().getRedisClient().connect();
            RedisHashDataProcess.process(originConfig, redisConnectionFuture, redisData);

        });
    }

}
