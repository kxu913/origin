package com.origin.framework.redis.factory;

import com.origin.framework.core.bean.OriginVertxContext;
import com.origin.framework.redis.constants.DataType;
import com.origin.framework.spi.RedisShardingAlgorithm;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
public class RedisShardingFactory<T extends OriginVertxContext> {

    private final T vc;
    private final Map<String, Redis> REDIS_MAP;

    private final RedisShardingAlgorithm redisShardingAlgorithm;


    public RedisShardingFactory(T vc, RedisShardingAlgorithm redisShardingAlgorithm) {
        REDIS_MAP = new HashMap<>();
        this.vc = vc;
        this.redisShardingAlgorithm = redisShardingAlgorithm;


    }


    public Future<RedisShardingFactory<T>> loadRedisConfig() {
        return Future.future(ar -> {
            ConfigRetriever.create(vc.getVertx())
                    .getConfig()
                    .onComplete(arJson -> {
                        if (arJson.succeeded()) {
                            JsonObject config = arJson.result();
                            Iterator<Map.Entry<String, Object>> iterator = config.stream().iterator();
                            while (iterator.hasNext()) {
                                var entry = iterator.next();
                                if (entry.getKey().startsWith("redis")) {
                                    REDIS_MAP.put(entry.getKey(),
                                            Redis.createClient(vc.getVertx(), new RedisOptions(config.getJsonObject(entry.getKey()))));
                                }
                            }
                            ar.complete(this);
                        } else {
                            log.error("get config error.", arJson.cause());
                            ar.fail(arJson.cause());
                        }
                    });
        });

    }

    /**
     * Executes a request in Redis which get according to sharding algorithm configuration and returns a Future<Response> object.
     *
     * @param request     The Redis request object.
     * @param shardingKey The sharding key for the request.
     * @return A Future<Response> representing the result of the asynchronous computation.
     */
    public Future<Response> execute(Request request, String shardingKey) {
        // Obtain the actual key based on the provided sharding key.
        String actualKey = redisShardingAlgorithm == null ? "redis" : redisShardingAlgorithm.lookFor(shardingKey);

        // Return a Future that asynchronously computes the execution result.
        return Future.future(ar ->
                // Connect to the Redis instance using the actual key and send the request.
                REDIS_MAP.get(actualKey).connect()
                        .onSuccess(connection -> connection.send(request)
                                .onSuccess(ar::complete)
                                .onFailure(ar::fail))
                        .onFailure(ar::fail));
    }

    /**
     * Retrieves all/partial data from Redis and returns a Future<?> object.
     *
     * @param dataType The type of data to retrieve, possible values are "SET" or "HASH".
     * @param index    The name of the data index.
     * @param filter   An optional filtering function used to filter the result data, apply filter login to data type is "HASH" and value is json structure.
     * @return A Future<?> object representing the result of an asynchronous computatio.
     * - If the data type is "SET" then the result is a List<String> object.
     * - If the data type is "HASH" then the result is a List<JsonObject> object.
     */
    public Future<?> retrieveData(String dataType, String index, @Nullable Function<JsonObject, Boolean> filter) {
        DataType dataTypeEnum = DataType.valueOf(dataType.toUpperCase());
        if (dataTypeEnum == DataType.SET) {
            return retrieveSetData(index);
        } else {
            return retrieveHashData(index, filter);
        }


    }

    private Future<List<String>> retrieveSetData(String index) {
        Request dataSizeRequest = Request.cmd(Command.SCARD).arg(index);
        Request getAllRequest = Request.cmd(Command.SMEMBERS).arg(index);

        List<Future<ResponseWithFlag>> futureList = new ArrayList<>();

        return Future.future(ar -> {
            REDIS_MAP.forEach((k, redis) ->
                    futureList.add(
                            redis.connect()
                                    .compose(connection ->
                                            getResponseWithFlagFuture(index, DataType.SET, k, connection, dataSizeRequest, getAllRequest))));
            Future.all(futureList)
                    .onSuccess(cr -> {
                        handleSetResponse(ar, cr);
                    })
                    .onFailure(err -> {
                        log.error(err.getMessage(), err);
                        ar.fail(err);
                    });
        });


    }

    private Future<List<JsonObject>> retrieveHashData(String index, @Nullable Function<JsonObject, Boolean> filter) {
        Request dataSizeRequest = Request.cmd(Command.HLEN).arg(index);
        Request getAllRequest = Request.cmd(Command.HGETALL).arg(index);
        List<Future<ResponseWithFlag>> futureList = new ArrayList<>();

        return Future.future(ar -> {
            REDIS_MAP.forEach((k, redis) ->
                    futureList.add(
                            redis.connect()
                                    .compose(connection ->
                                            getResponseWithFlagFuture(index, DataType.HASH, k, connection, dataSizeRequest, getAllRequest)))

            );
            Future.all(futureList)
                    .onSuccess(cr -> {
                        handleHashResponse(ar, cr, filter);
                    })
                    .onFailure(err -> {
                        log.error(err.getMessage(), err);
                        ar.fail(err);
                    });
        });


    }

    private Future<ResponseWithFlag> getResponseWithFlagFuture(String index, DataType dataType, String redisKey, RedisConnection connection, Request dataSizeRequest, Request getAllRequest) {
        return connection.send(dataSizeRequest)
                .compose(response -> {
                    int total = response.toInteger();
                    log.info("The {} in {} contains {} records", index, redisKey, total);
                    List<Response> responseList = new ArrayList<>();
                    if (total < redisShardingAlgorithm.optimizeThreshold()) {
                        return connection.send(getAllRequest)
                                .compose(x -> {
                                    responseList.add(x);
                                    return Future.future(xar -> {
                                        xar.complete(new ResponseWithFlag(false, responseList));
                                    });
                                });

                    } else {
                        log.info("optimize search all, use cursor instead of getting all.");
                        return batchRetrieveData(connection, dataType, index, responseList)
                                .compose(x -> Future.future(xar -> {
                                    xar.complete(new ResponseWithFlag(true, responseList));
                                }));

                    }
                });
    }

    private Future<Response> batchRetrieveData(RedisConnection connection, DataType dataType, String index, List<Response> responseList) {

        Request scanRequest = dataType == DataType.HASH ? Request.cmd(Command.HSCAN).arg(index) : Request.cmd(Command.SSCAN).arg(index);
        Future<Response> firstFuture = connection.send(scanRequest
                .arg(0)
                .arg("match")
                .arg("*")
                .arg("count").arg(redisShardingAlgorithm.batchSize()));
        return firstFuture
                .compose(response -> scanHashData(connection, dataType, index, response, responseList));
    }

    private Future<Response> scanHashData(RedisConnection connection,
                                          DataType dataType,
                                          String index,
                                          Response response,
                                          List<Response> responseList) {
        int cursor = response.get(0).toInteger();
        responseList.add(response.get(1));
        if (log.isDebugEnabled()) {
            log.debug("cursor move to {}", cursor);
        }

        if (cursor == 0) {
            log.info("cursor reach end.");
            return Future.succeededFuture(response);
        } else {
            Request scanRequest = dataType == DataType.HASH ? Request.cmd(Command.HSCAN).arg(index) :
                    Request.cmd(Command.SSCAN).arg(index);
            Request request = scanRequest.arg(cursor).arg("match").arg("*").arg("count").arg(redisShardingAlgorithm.batchSize());
            Future<Response> responseFuture = connection.send(request);
            return responseFuture
                    .compose(r -> scanHashData(connection, dataType, index, r, responseList))
                    .onFailure(err -> {
                        log.error(err.getMessage(), err);
                    });
        }


    }

    private void handleHashResponse(Promise<List<JsonObject>> ar, CompositeFuture cr, Function<JsonObject, Boolean> filter) {
        List<JsonObject> results = new ArrayList<>();
        List<ResponseWithFlag> responses = cr.list();
        responses.forEach(rf -> {
            rf.response.forEach(response -> {
                if (rf.flag) {
                    for (int i = 0; i < response.size(); i += 2) {
                        boolean isValidJsonString = false;
                        try {
                            JsonObject jsonObject = new JsonObject(response.get(i + 1).toString());
                            if (filter != null && filter.apply(jsonObject)) {
                                log.info("{} had been filtered.", jsonObject);

                            } else {
                                results.add(new JsonObject().put(response.get(i).toString(), jsonObject));
                            }

                            isValidJsonString = true;
                        } catch (Exception ignored) {

                        }
                        if (!isValidJsonString) {
                            results.add(new JsonObject().put(response.get(i).toString(), response.get(i + 1).toString()));
                        }
                    }

                } else {
                    AtomicInteger i = new AtomicInteger();
                    response.forEach(entry -> {
                        i.getAndIncrement();
                        boolean isValidJsonString = false;
                        try {
                            JsonObject jsonObject = new JsonObject(entry.get(1).toString());
                            if (filter != null && filter.apply(jsonObject)) {
                                log.info("{} had been filtered.", jsonObject);

                            } else {
                                results.add(new JsonObject().put(entry.get(0).toString(), jsonObject));
                            }

                            isValidJsonString = true;
                        } catch (Exception ignored) {

                        }
                        if (!isValidJsonString) {
                            results.add(new JsonObject().put(entry.get(0).toString(), entry.get(1).toString()));

                        }

                    });
                }


            });


        });
        ar.complete(results);
    }

    private void handleSetResponse(Promise<List<String>> ar, CompositeFuture cr) {
        List<String> results = new ArrayList<>();
        List<ResponseWithFlag> responses = cr.list();
        responses.forEach(rf -> {
            rf.response.forEach(response ->
                    response.forEach(entry -> {
                        results.add(entry.toString());
                    })
            );
        });
        ar.complete(results);
    }

    record ResponseWithFlag(boolean flag, List<Response> response) {
    }

}
