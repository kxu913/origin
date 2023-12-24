package com.origin.starter.app.core;



import com.origin.framework.core.bean.OriginVertxContext;
import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.spi.OriginTask;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class OriginTaskFactory {

    private final List<OriginTask> routers = new ArrayList<>();
    private final OriginVertxContext vc;
    private final OriginConfig cf;


    public OriginTaskFactory(OriginVertxContext vc, OriginConfig cf) {
        this.vc = vc;
        this.cf = cf;
        ServiceLoader<OriginTask> loader = ServiceLoader.load(OriginTask.class);
        loader.forEach(routers::add);
    }

    public void register() {
        routers.forEach(r -> r.run(this.vc, this.cf));

    }

}
