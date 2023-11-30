package com.origin.starter.web.handler;

import com.origin.starter.web.domain.OriginVertxContext;
import io.vertx.core.Future;
import io.vertx.core.shareddata.Lock;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LockHandler {
    private static final String DEFAULT_LOCK_KEY = "lock";

    /**
     * Run the specified operation with the given lock and timeout in the provided context.
     *
     * @param context    the origin vertx context
     * @param ctx        the routing context
     * @param lockKey    the key for the lock
     * @param expireTime the timeout for acquiring the lock
     * @param fn         the operation to run
     */
    public static void runWithLockAndTimeout(OriginVertxContext context, RoutingContext ctx, String lockKey, long expireTime, Runnable fn) {
        // Get the lock with the specified lock key and timeout
        Future<Lock> future = context.getSharedData().getLockWithTimeout(lockKey, expireTime);
        // Handle the result of acquiring the lock asynchronously. If the lock is acquired successfully, run the specified operation and release the lock.
        AsyncResultHandler.handleFuture(future, ctx, lock -> {
            fn.run();
            lock.release();
        });
    }


    /**
     * Run the specified operation with a lock in the given context.
     *
     * @param context the origin Vertx context
     * @param ctx     the routing context
     * @param lockKey the key of the lock
     * @param fn      the runnable object to be executed
     */
    public static void runWithLock(OriginVertxContext context, RoutingContext ctx, String lockKey, Runnable fn) {
        // Get the lock from the shared data in the given context
        Future<Lock> future = context.getSharedData().getLock(lockKey);
        // Handle the async result of getting the lock
        AsyncResultHandler.handleFuture(future, ctx, lock -> {
            // Run the specified operation
            fn.run();
            // Release the lock
            lock.release();
        });
    }


    /**
     * Run the specified code block with a lock object from the shared data in the origin Vertx context.
     *
     * @param context the origin Vertx context
     * @param ctx     the routing context
     * @param fn      the code block to run
     */
    public static void runWithLock(OriginVertxContext context, RoutingContext ctx, Runnable fn) {
        // Get the lock object from the shared data in the origin Vertx context with the default lock key
        Future<Lock> future = context.getSharedData().getLock(DEFAULT_LOCK_KEY);
        // Handle the asynchronous result of acquiring the lock
        AsyncResultHandler.handleFuture(future, ctx, lock -> {
            // Run the specified code block
            fn.run();
            // Release the lock object
            lock.release();
        });
    }

}
