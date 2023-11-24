package com.origin.starter.web.handler;

import com.origin.starter.web.exception.InvalidResponseException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class AsyncResultHandler {
    /**
     * simplify operations about how to handle an async result, wrapped error handling.
     *
     * @param ar,   an async result that contains object from some operations.
     * @param ctx,  RoutingContext.
     * @param func, the function that handle the success object.
     * @param <T>   success object that used to handle.
     */
    public static <T> void handleAsyncResult(AsyncResult<T> ar, RoutingContext ctx, Consumer<T> func) {
        if (ar.succeeded()) {
            func.accept(ar.result());
        } else {
            log.error(ar.cause().getMessage(), ar.cause());
            ctx.fail(500, ar.cause());
        }

    }

    /**
     * simplify operations about how to handle an async result, wrapped error handling,return an object.
     *
     * @param ar   an async result that contains object from some operations.
     * @param func the function that handle the success object.
     * @param <T>  success object that used to handle.
     * @param <R>  response object.
     * @return R
     */
    public static <T, R> R handleAsyncResultWithReturn(AsyncResult<T> ar, Function<T, R> func) {
        if (ar.succeeded()) {
            return func.apply(ar.result());
        } else {
            throw new InvalidResponseException(ar.cause().getMessage(), ar.cause());
        }


    }

    /**
     * simplify operations about how to handle a future, wrapped error handling.
     *
     * @param future, a future that contains object from some operations.
     * @param ctx,    RoutingContext.
     * @param func,   the function that handle the success object.
     * @param <T>     success object that used to handle
     */
    public static <T> void handleFuture(Future<T> future, RoutingContext ctx, Consumer<T> func) {
        future.onComplete(ar -> {
            handleAsyncResult(ar, ctx, func);
        }).onFailure(err -> {
            log.error(err.getMessage(), err);
            ctx.fail(500, err);
        });

    }

    /**
     * simplify operations about how to handle a future, wrapped error handling, return an object.
     *
     * @param future a future that contains object from some operations.
     * @param func   the function that handle the success object.
     * @param <T>    success object that used to handle as input.
     * @param <R>    response object.
     * @return R
     */
    public static <T, R> void handleFutureWithReturn(Future<T> future, RoutingContext ctx, Function<T, R> func) {
        future.onComplete(ar -> {
            ctx.json(handleAsyncResultWithReturn(ar, func));
        }).onFailure(err -> {
            log.error(err.getMessage(), err);
            ctx.fail(500, err);
        });

    }


}
