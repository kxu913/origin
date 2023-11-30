package com.kevin.sample.vertx.blog.router;


import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.domain.OriginVertxContext;
import com.origin.starter.web.handler.AsyncResultHandler;
import com.origin.starter.web.handler.LockHandler;
import com.origin.starter.web.spi.OriginRouter;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class LockRouter implements OriginRouter {
    @Override
    public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {

        // a simple example that handle ctx in handler method.
        originVertxContext.getRouter().get("/lock")
                .handler(ctx -> {
                    LockHandler.runWithLock(originVertxContext, ctx, () -> {

                        Future<String> awaitFuture = originVertxContext.getVertx().executeBlocking(() -> {
                            Thread.sleep(2000L);
                            return "I got the lock.";
                        });
                        AsyncResultHandler.handleFuture(awaitFuture, ctx, ctx::end);
                    });

                });


    }
}
