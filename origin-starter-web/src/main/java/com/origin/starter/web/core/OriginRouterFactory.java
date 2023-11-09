package com.origin.starter.web.core;


import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.domain.OriginVertxContext;
import com.origin.starter.web.spi.OriginRouter;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class OriginRouterFactory {

    private final List<OriginRouter> routers = new ArrayList<>();
    private final OriginVertxContext vc;
    private final OriginConfig cf;


    public OriginRouterFactory(OriginVertxContext vc, OriginConfig cf) {
        this.vc = vc;
        this.cf = cf;
        ServiceLoader<OriginRouter> loader = ServiceLoader.load(OriginRouter.class);
        loader.forEach(routers::add);
    }

    public void register() {
        routers.forEach(r -> r.router(this.vc, this.cf));

    }

}
