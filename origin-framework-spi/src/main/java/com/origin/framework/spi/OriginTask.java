package com.origin.framework.spi;


import com.origin.framework.core.bean.OriginVertxContext;
import com.origin.framework.core.bean.OriginConfig;

public interface OriginTask {
    void run(OriginVertxContext vc, OriginConfig cf);

    default boolean enable() {
        return true;
    }
}
