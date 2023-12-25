package com.origin.starter.web.router;

import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.spi.OriginRouter;
import com.origin.framework.spi.RedisData;
import com.origin.starter.common.data.RedisSetDataProcess;
import com.origin.starter.web.OriginWebApplication;
import io.vertx.core.Future;
import io.vertx.redis.client.RedisConnection;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

@Slf4j
public class RedisSetDataRouter implements OriginRouter {
    @Override
    public void router(OriginWebVertxContext originWebVertxContext, OriginConfig originConfig) {
        ServiceLoader<RedisData> loader = ServiceLoader.load(RedisData.class);
        loader.forEach(redisData -> {
            Future<RedisConnection> redisConnectionFuture = OriginWebApplication.getBeanFactory().getRedisClient().connect();
            RedisSetDataProcess.process(originConfig, redisConnectionFuture, redisData);
        });
    }

}
