package com.origin.framework.spi;


import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.core.bean.OriginWebVertxContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;

public interface OriginRouter {
    public void router(OriginWebVertxContext vc, OriginConfig cf);

    default void errorHandler(RoutingContext ctx, HttpResponseStatus status, Throwable error) {
        ctx.response().setStatusCode(status.code());
        ctx.end(error.getMessage());
    }
}
