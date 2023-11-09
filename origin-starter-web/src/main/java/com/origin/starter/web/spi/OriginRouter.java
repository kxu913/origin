package com.origin.starter.web.spi;

import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.domain.OriginVertxContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;

public interface OriginRouter {
    public void router(OriginVertxContext vc, OriginConfig cf);

    default void errorHandler(RoutingContext ctx, HttpResponseStatus status, Throwable error) {
        ctx.response().setStatusCode(status.code());
        ctx.end(error.getMessage());
    }
}
