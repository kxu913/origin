package com.origin.starter.web.core;


import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.spi.DBData;
import com.origin.framework.spi.ESData;
import com.origin.framework.spi.OriginRouter;
import com.origin.framework.spi.RedisData;
import com.origin.starter.web.router.DBDataRouter;
import com.origin.starter.web.router.ESDataRouter;
import com.origin.starter.web.router.RedisHashDataRouter;
import com.origin.starter.web.router.RedisSetDataRouter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

@Slf4j
public class OriginRouterFactory {

    private final List<OriginRouter> routers = new ArrayList<>();
    private final OriginWebVertxContext vc;
    private final OriginConfig cf;


    public OriginRouterFactory(OriginWebVertxContext vc, OriginConfig cf) {
        this.vc = vc;
        this.cf = cf;
        ServiceLoader<OriginRouter> loader = ServiceLoader.load(OriginRouter.class);
        ServiceLoader<DBData> dbDataLoader = ServiceLoader.load(DBData.class);
        ServiceLoader<ESData> esDataLoader = ServiceLoader.load(ESData.class);
        ServiceLoader<RedisData> redisDataLoader = ServiceLoader.load(RedisData.class);
        if (dbDataLoader.findFirst().isPresent()) {
            routers.add(new DBDataRouter());
        }
        if (esDataLoader.findFirst().isPresent()) {
            routers.add(new ESDataRouter());
        }
        if (redisDataLoader.findFirst().isPresent()) {
            RedisData rd = redisDataLoader.findFirst().get();
            routers.add(new RedisSetDataRouter());
            if (rd.needCacheObject()) {
                routers.add(new RedisHashDataRouter());
            }

        }
        loader.forEach(routers::add);
    }

    public void register() {
        log.info("**** routers {} had been registered.", routers);
        routers.forEach(r -> r.router(this.vc, this.cf));

    }

}
