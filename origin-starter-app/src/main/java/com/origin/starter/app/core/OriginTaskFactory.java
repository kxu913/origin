package com.origin.starter.app.core;


import com.origin.framework.core.bean.OriginVertxContext;
import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.spi.*;
import com.origin.starter.app.task.DBDataTask;
import com.origin.starter.app.task.ESDataTask;
import com.origin.starter.app.task.RedisSetDataTask;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

@Slf4j
public class OriginTaskFactory {

    private final List<OriginTask> tasks = new ArrayList<>();
    private final OriginVertxContext vc;
    private final OriginConfig cf;


    public OriginTaskFactory(OriginVertxContext vc, OriginConfig cf) {
        this.vc = vc;
        this.cf = cf;
        ServiceLoader<OriginTask> loader = ServiceLoader.load(OriginTask.class);

        ServiceLoader<DBData> dbDataLoader = ServiceLoader.load(DBData.class);
        ServiceLoader<ESData> esDataLoader = ServiceLoader.load(ESData.class);
        ServiceLoader<RedisData> redisDataLoader = ServiceLoader.load(RedisData.class);
        if (dbDataLoader.findFirst().isPresent()) {
            tasks.add(new DBDataTask());
        }
        if (esDataLoader.findFirst().isPresent()) {
            tasks.add(new ESDataTask());
        }
        if (redisDataLoader.findFirst().isPresent()) {
            RedisData rd = redisDataLoader.findFirst().get();
            tasks.add(new RedisSetDataTask());
            if (rd.needCacheObject()) {
                tasks.add(new RedisSetDataTask());
            }

        }
        loader.forEach(tasks::add);
    }

    public void register() {
        log.info("**** tasks {} had been registered.", tasks);
        tasks.forEach(r -> {
            if (r.enable()) {
                r.run(this.vc, this.cf);
            }
        });

    }

}
