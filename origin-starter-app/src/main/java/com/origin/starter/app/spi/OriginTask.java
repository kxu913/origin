package com.origin.starter.app.spi;

import com.origin.starter.app.domain.OriginAppConfig;
import com.origin.starter.app.domain.OriginAppVertxContext;

public interface OriginTask {
    public void run(OriginAppVertxContext vc, OriginAppConfig cf);
}
