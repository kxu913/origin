package com.origin.framework.file.util;


import com.origin.framework.core.bean.OriginVertxContext;
import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.core.handler.AsyncResultHandler;
import com.origin.framework.file.domain.ComposeRequest;
import com.origin.framework.file.domain.FileHandlerRequest;
import com.origin.framework.file.domain.ResultReport;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.RedisConnection;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
public class FileUtil {
    /**
     * Read from the specified file with Redis, return the result report include statistics information to http response.
     *
     * @param originVertxContext    the context of the original WebVertx
     * @param redisConnectionFuture the future object of the Redis connection, get data from redis or write data to redis.
     * @param ctx                   the routing context
     * @param request               the file handler request
     * @param fn                    the consumer object of ComposeRequest
     */
    public static void readFileWithRedis(OriginWebVertxContext originVertxContext, Future<RedisConnection> redisConnectionFuture, RoutingContext ctx, FileHandlerRequest request, Consumer<ComposeRequest> fn) {
        Optional<String> file = Optional.of(request.getFile());
        ResultReport resultReport = new ResultReport().start();
        AsyncResultHandler.handleFuture(redisConnectionFuture, ctx, connection -> {
            Future<AsyncFile> asyncFileFuture = originVertxContext.getFs().open(file.get(), new OpenOptions());
            AsyncResultHandler.handleFuture(asyncFileFuture, ctx, asyncFile -> {

                asyncFile.handler(createRecordParser(request, fn, connection, resultReport, null)).endHandler(v -> {
                    if (connection != null) {
                        connection.close();
                    }
                    asyncFile.close();
                    ResultReport report = resultReport.end();
                    log.info("statistics info is {}", report);
                    if (!ctx.response().ended()) {
                        ctx.json(report);
                    }
                });

            });
        });

    }

    /**
     * Read from the specified file with Redis, most usage is read file and load it into redis.
     *
     * @param originVertxContext    the context of the original WebVertx
     * @param redisConnectionFuture the future object of the Redis connection, get data from redis or write data to redis.
     * @param request               the file handler request
     * @param fn                    the consumer object of ComposeRequest
     */
    public static void readFileWithRedis(OriginVertxContext originVertxContext, Future<RedisConnection> redisConnectionFuture, FileHandlerRequest request, Consumer<ComposeRequest> fn) {
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
                    ResultReport report = resultReport.end();
                    log.info("statistics info is {}", report);
                });

            });
        });

    }

    /**
     * Read from the specified file with Redis and write to the destination file, return the result report include statistics information to http response.
     *
     * @param originVertxContext    the context of the original WebVertx
     * @param redisConnectionFuture the future object of the Redis connection, get data from redis or write data to redis.
     * @param ctx                   the routing context
     * @param request               the file handler request
     * @param fn                    the consumer object of ComposeRequest
     */
    public static void readWriteFileWithRedis(OriginWebVertxContext originVertxContext,
                                              Future<RedisConnection> redisConnectionFuture,
                                              RoutingContext ctx,
                                              FileHandlerRequest request,
                                              Consumer<ComposeRequest> fn) {
        Optional<String> file = Optional.of(request.getFile());
        Optional<String> destFile = Optional.of(request.getDestFile());
        ResultReport resultReport = new ResultReport().start();
        AsyncResultHandler.handleFuture(redisConnectionFuture, ctx, connection -> {
            Buffer buffer = Buffer.buffer();
            Future<AsyncFile> asyncFileFuture = originVertxContext.getFs().open(file.get(), new OpenOptions());
            AsyncResultHandler.handleFuture(asyncFileFuture, ctx, asyncFile -> {
                // Set handler for the asynchronous file
                asyncFile.handler(createRecordParser(request, fn, connection, resultReport, buffer)).endHandler(v -> {
                    // Write file to destination
                    originVertxContext.getFs().writeFile(destFile.get(), buffer, e -> {
                        connection.close();
                        asyncFile.close();
                        ResultReport report = resultReport.additionalInfo("destFile", destFile.get()).end();
                        log.info("statistics info is {}", report);
                        if (!ctx.response().ended()) {
                            ctx.json(report);
                        }
                    });
                });
            });
        });
    }

    /**
     * Read from the specified file with Redis and write to the destination file.
     *
     * @param originVertxContext    the context of the original WebVertx
     * @param redisConnectionFuture the future object of the Redis connection, get data from redis or write data to redis.
     * @param request               the file handler request
     * @param fn                    the consumer object of ComposeRequest
     */
    public static void readWriteFileWithRedis(OriginWebVertxContext originVertxContext,
                                              Future<RedisConnection> redisConnectionFuture,

                                              FileHandlerRequest request,
                                              Consumer<ComposeRequest> fn) {
        Optional<String> file = Optional.of(request.getFile());
        Optional<String> destFile = Optional.of(request.getDestFile());
        ResultReport resultReport = new ResultReport().start();
        AsyncResultHandler.handleFuture(redisConnectionFuture, connection -> {
            Buffer buffer = Buffer.buffer();
            Future<AsyncFile> asyncFileFuture = originVertxContext.getFs().open(file.get(), new OpenOptions());
            AsyncResultHandler.handleFuture(asyncFileFuture, asyncFile -> {

                // Set handler for the asynchronous file
                asyncFile.handler(createRecordParser(request, fn, connection, resultReport, buffer)).endHandler(v -> {
                    log.info("buffer size is {}", buffer.getBytes().length);
                    // Write file to destination
                    originVertxContext.getFs().writeFile(destFile.get(), buffer, e -> {
                        connection.close();
                        asyncFile.close();
                        ResultReport report = resultReport.additionalInfo("destFile", destFile.get()).end();
                        log.info("statistics info is {}", report);

                    });
                });
            });
        });
    }


    private static RecordParser createRecordParser(FileHandlerRequest request, Consumer<ComposeRequest> fn, RedisConnection connection, ResultReport resultReport, Buffer buffer) {
        return RecordParser.newDelimited(request.getDelimiter(), bufferLine -> {
            String line = bufferLine.toString(request.getEncode());
            if (request.isIgnoreFirstLine() && resultReport.getTotalSize().get() > 0) {
                try {
                    fn.accept(new ComposeRequest(line).withConnection(connection).withResultReport(resultReport).withBuffer(buffer));
                } catch (Exception e) {
                    log.error("handle line failed, caused by {}", e.getMessage(), e);
                    resultReport.getErrorSize().incrementAndGet();
                }

            } else {
                try {
                    fn.accept(new ComposeRequest(line).withConnection(connection).withResultReport(resultReport).withBuffer(buffer));
                } catch (Exception e) {
                    log.error("handle line failed, caused by {}", e.getMessage(), e);
                    resultReport.getErrorSize().incrementAndGet();
                }
            }

            resultReport.getTotalSize().incrementAndGet();

        });
    }

}
