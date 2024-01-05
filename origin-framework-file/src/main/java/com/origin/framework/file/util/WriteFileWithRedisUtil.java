package com.origin.framework.file.util;


import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.core.handler.AsyncResultHandler;
import com.origin.framework.file.domain.HashDataRequest;
import com.origin.framework.file.domain.ResultReport;
import com.origin.framework.file.domain.WriteFileWithRedisRequest;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.Request;
import io.vertx.redis.client.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * it's a util class that used to write file from another file and use redis to do some filter.
 *
 * @author Kevin Xu
 */
@Slf4j
public class WriteFileWithRedisUtil {
    /**
     * use to read file and filter by redis, finally write to dest file.
     *
     * @param originVertxContext    origin context used to operate files.
     * @param redisConnectionFuture redis connection future.
     * @param ctx                   create response of http request.
     * @param request               at least need specify origin file and dest file.
     * @param fn                    generate redis request by line of origin file.
     * @param consumer              first parameter use to add more detail of resultreport, second parameter use to consume response from redis.
     */
    public static void writeFileWithRedis(OriginWebVertxContext originVertxContext,
                                          Future<RedisConnection> redisConnectionFuture,
                                          RoutingContext ctx,
                                          WriteFileWithRedisRequest request,
                                          Function<String, Request> fn, BiConsumer<ResultReport, Response> consumer) {
        Optional<String> file = Optional.of(request.getFile());
        Optional<String> destFile = Optional.of(request.getDestFile());
        ResultReport resultReport = new ResultReport().start();
        AsyncResultHandler.handleFuture(redisConnectionFuture, ctx, connection -> {
            Future<AsyncFile> asyncFileFuture = originVertxContext.getFs().open(file.get(), new OpenOptions());
            AsyncResultHandler.handleFuture(asyncFileFuture, ctx, asyncFile -> {

                asyncFile.handler(createRecordParser(request, fn, connection, resultReport, null)).endHandler(v -> {
                    if (connection != null) {
                        connection.close();
                    }
                    asyncFile.close();
                    Future.all(request.getFutures())
                            .onSuccess(cfs -> {
                                List<Response> list = cfs.list();

                                list.forEach(response ->
                                        consumer.accept(resultReport, response)
                                );
                                originVertxContext.getFs().writeFile(destFile.get(), request.getBuffer(), x -> {
                                    ResultReport report = resultReport.additionalInfo("destFile", destFile.get()).end();
                                    log.info("statistics info is {}", report);
                                    if (!ctx.response().ended()) {
                                        ctx.json(report);
                                    }
                                });
                            })
                            .onFailure(err -> {
                                log.error(err.getMessage(), err);
                                ctx.fail(500, err);
                            });

                });

            });
        });

    }

    /**
     * use to read file and filter by redis, finally write to dest file.
     *
     * @param originVertxContext    origin context used to operate files.
     * @param redisConnectionFuture redis connection future.
     * @param request               at least need specify origin file and dest file.
     * @param fn                    generate redis request by line of origin file.
     * @param consumer              first parameter use to add more detail of resultreport, second parameter use to consume response from redis.
     */
    public static void writeFileWithRedis(OriginWebVertxContext originVertxContext,
                                          Future<RedisConnection> redisConnectionFuture,
                                          WriteFileWithRedisRequest request,
                                          Function<String, Request> fn,
                                          BiConsumer<ResultReport, Response> consumer) {
        Optional<String> file = Optional.of(request.getFile());
        ResultReport resultReport = new ResultReport().start();
        AsyncResultHandler.handleFuture(redisConnectionFuture, connection -> {
            Future<AsyncFile> asyncFileFuture = originVertxContext.getFs().open(file.get(), new OpenOptions());
            AsyncResultHandler.handleFuture(asyncFileFuture, asyncFile -> {

                asyncFile.handler(createRecordParser(request, fn, connection, resultReport, null)).endHandler(v -> {
                    if (connection != null) {
                        connection.close();
                    }
                    asyncFile.close();
                    Future.all(request.getFutures())
                            .onSuccess(cfs -> {
                                List<Response> list = cfs.list();

                                list.forEach(response ->
                                        consumer.accept(resultReport, response)
                                );
                                originVertxContext.getFs().writeFile(request.getDestFile(), request.getBuffer(), x -> {
                                    ResultReport report = resultReport.additionalInfo("destFile", request.getDestFile()).end();
                                    log.info("statistics info is {}", report);

                                });
                            })
                            .onFailure(err -> {
                                log.error(err.getMessage(), err);
                            });

                });

            });
        });

    }

    public static void writeFileFromHashData(RoutingContext ctx,
                                             RedisConnection connection,
                                             String index,
                                             String pattern,
                                             int batchSize,
                                             Consumer<HashDataRequest> fn) {
        Request request = Request.cmd(Command.HSCAN).arg(index).arg(0).arg("match").arg(pattern).arg("count").arg(batchSize);
        Future<Response> firstFuture = connection.send(request);

        ResultReport resultReport = new ResultReport().start();
        Future<Response> lastFuture = firstFuture.compose(response -> {
            resultReport.getFileIndex().incrementAndGet();
            fn.accept(new HashDataRequest(response).withResultReport(resultReport));
            return retrieveHashData(connection, index, pattern, batchSize, response, resultReport, fn);
        });
        AsyncResultHandler.handleFuture(lastFuture, ctx, v -> {
            connection.close();
            ctx.json(resultReport.end());
        });

    }


    private static RecordParser createRecordParser(WriteFileWithRedisRequest request, Function<String, Request> fn, RedisConnection connection, ResultReport resultReport, Buffer buffer) {
        return RecordParser.newDelimited(request.getDelimiter(), bufferLine -> {
            String line = bufferLine.toString(request.getEncode());
            if (request.isIgnoreFirstLine() && resultReport.getTotalSize().get() > 0) {
                try {
                    request.getFutures().add(connection.send(fn.apply(line)));
                } catch (Exception e) {
                    log.error("handle line failed, caused by {}", e.getMessage(), e);
                    resultReport.getErrorSize().incrementAndGet();
                }
            } else {
                try {
                    request.getFutures().add(connection.send(fn.apply(line)));
                } catch (Exception e) {
                    log.error("handle line failed, caused by {}", e.getMessage(), e);
                    resultReport.getErrorSize().incrementAndGet();
                }
            }


            resultReport.getTotalSize().incrementAndGet();

        });
    }

    private static Future<Response> retrieveHashData(RedisConnection connection,
                                                     String index,
                                                     String pattern,
                                                     int batchSize,
                                                     Response response,
                                                     ResultReport resultReport,
                                                     Consumer<HashDataRequest> fn) {
        int cursor = response.get(0).toInteger();
        log.info("cursor move to {}", cursor);
        if (cursor == 0) {
            log.info("cursor reach end.");
            resultReport.getFileIndex().incrementAndGet();
            fn.accept(new HashDataRequest(response).withResultReport(resultReport));
            return Future.succeededFuture(response);
        } else {
            resultReport.getFileIndex().incrementAndGet();
            fn.accept(new HashDataRequest(response).withResultReport(resultReport));
            Request request = Request.cmd(Command.HSCAN).arg(index).arg(cursor).arg("match").arg(pattern).arg("count").arg(batchSize);
            Future<Response> responseFuture = connection.send(request);
            return responseFuture.compose(r -> retrieveHashData(connection, index, pattern, batchSize, r, resultReport, fn));
        }


    }

}
