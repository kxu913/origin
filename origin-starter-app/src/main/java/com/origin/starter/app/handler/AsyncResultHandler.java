package com.origin.starter.app.handler;


import com.origin.starter.app.exception.InvalidResponseException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class AsyncResultHandler {
    /**
     * simplify operations about how to handle an async result, add error log by default.
     *
     * @param ar,   an async result that contains object from some operations.
     * @param func, the function that handle the success object.
     * @param <T>   success object that used to handle.
     */
    public static <T> void handleAsyncResult(AsyncResult<T> ar, Function<T, Void> func) {
        if (ar.succeeded()) {
            func.apply(ar.result());
        } else {
            log.error(ar.cause().getMessage(), ar.cause());
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
     * @throws InvalidResponseException
     */
    public static <T, R> R handleAsyncResultWithReturn(AsyncResult<T> ar, Function<T, R> func) {
        if (ar.succeeded()) {
            return func.apply(ar.result());
        } else {
            log.error(ar.cause().getMessage(), ar.cause());
            throw new InvalidResponseException(ar.cause().getMessage(), ar.cause());
        }


    }

    /**
     * simplify operations about how to handle a future, add error log by default.
     *
     * @param future, a future that contains object from some operations.
     * @param func,   the function that handle the success object.
     * @param <T>     success object that used to handle
     */
    public static <T> void handleFuture(Future<T> future, Function<T, Void> func) {
        future.onComplete(ar -> {
            handleAsyncResult(ar, func);
        }).onFailure(err -> {
            log.error(err.getMessage(), err);
        });

    }


}
