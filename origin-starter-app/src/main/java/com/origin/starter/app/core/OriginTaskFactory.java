package com.origin.starter.app.core;


import com.origin.starter.app.domain.OriginAppConfig;
import com.origin.starter.app.domain.OriginAppVertxContext;
import com.origin.starter.app.spi.OriginTask;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class OriginTaskFactory {

    private final List<OriginTask> routers = new ArrayList<>();
    private final OriginAppVertxContext vc;
    private final OriginAppConfig cf;


    public OriginTaskFactory(OriginAppVertxContext vc, OriginAppConfig cf) {
        this.vc = vc;
        this.cf = cf;
        ServiceLoader<OriginTask> loader = ServiceLoader.load(OriginTask.class);
        loader.forEach(routers::add);
    }

    public void register() {
        routers.forEach(r -> r.run(this.vc, this.cf));

    }

}
