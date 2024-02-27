package com.origin.framework.file.util;


import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.core.handler.AsyncResultHandler;
import com.origin.framework.file.domain.request.LoadFileRequest;
import com.origin.framework.file.domain.result.ResultReport;
import io.vertx.core.Future;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.Request;
import io.vertx.redis.client.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * it's a util class that used to load file to redis.
 *
 * @author Kevin Xu
 */
@Slf4j
public class LoadFileUtil {

    /**
     * Load file, return the result report include statistics information to http response.
     *
     * @param originVertxContext the context of the original WebVertx
     * @param ctx                the routing context
     * @param request            the file handler request
     * @param fn                 the function, accept line and return request,if you want to filter line, just return null.
     */
    public static void loadFile(OriginWebVertxContext originVertxContext,
                                RoutingContext ctx,
                                LoadFileRequest request,
                                Function<String, Request> fn) {
        try {
            Optional<String> file = Optional.of(request.getFile());
            ResultReport resultReport = new ResultReport().start();
            if (log.isDebugEnabled()) {
                log.debug("receive a request {} that load file to redis.", request);
            }
            Future<AsyncFile> asyncFileFuture = originVertxContext.getFs().open(file.get(), new OpenOptions());
            AsyncResultHandler.handleFuture(asyncFileFuture, ctx, asyncFile -> {

                asyncFile.handler(createRecordParser(request, fn, resultReport)).endHandler(v -> {
                    asyncFile.close();
                    request.getConnection().close();
                    ctx.json(resultReport);

                });


            });
        } catch (NullPointerException ne) {
            ctx.fail(400, ne);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ctx.fail(500, e);
        }


    }

    /**
     * Load file, log resultReport.
     *
     * @param originVertxContext the context of the original WebVertx
     * @param request            the file handler request
     * @param fn                 the function, accept line and return request, if you want to filter line, just return null.
     */
    public static void loadFile(OriginWebVertxContext originVertxContext,
                                LoadFileRequest request,
                                Function<String, Request> fn) {
        try {
            Optional<String> file = Optional.of(request.getFile());

            ResultReport resultReport = new ResultReport().start();
            if (log.isDebugEnabled()) {
                log.debug("receive a request {} that load file to redis.", request);
            }
            Future<AsyncFile> asyncFileFuture = originVertxContext.getFs().open(file.get(), new OpenOptions());
            AsyncResultHandler.handleFuture(asyncFileFuture, asyncFile -> {

                asyncFile.handler(createRecordParser(request, fn, resultReport)).endHandler(v -> {
                    asyncFile.close();
                    request.getConnection().close();
                    log.info("statistics info is {}", resultReport);

                });


            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


    }

    /**
     * Load file, use batch mode, batch size is 10,000, only support set data structure,
     * return the result report include statistics information to http response.
     *
     * @param originVertxContext the context of the original WebVertx
     * @param ctx                the routing context
     * @param request            the file handler request
     * @param fn                 the function, accept line and return key which store in redis set structure.
     */
    public static void batchLoadFile(OriginWebVertxContext originVertxContext,
                                     RoutingContext ctx,
                                     LoadFileRequest request,
                                     Function<String, String> fn) {
        try {
            Optional<String> file = Optional.of(request.getFile());
            Optional<String> redisIndex = Optional.of(request.getIndex());

            ResultReport resultReport = new ResultReport().start();
            if (log.isDebugEnabled()) {
                log.debug("receive a request {} that load file to redis.", request);
            }
            Future<AsyncFile> asyncFileFuture = originVertxContext.getFs().open(file.get(), new OpenOptions());
            List<Future<Response>> futureList = new ArrayList<>();
            AsyncResultHandler.handleFuture(asyncFileFuture, ctx, asyncFile -> {
                Optional<RedisConnection> connection = Optional.of(request.getConnection());
                AtomicInteger batchSize = new AtomicInteger(10000);
                AtomicInteger cursor = new AtomicInteger(0);
                List<String> keyList = new ArrayList<>(batchSize.get());
                RecordParser recordParser = createRecordParserForBatch(request, fn, cursor, batchSize, keyList, futureList, resultReport);

                asyncFile.handler(recordParser).endHandler(v -> {
                    asyncFile.close();
                    if (!keyList.isEmpty()) {
                        futureList.add(createBatchRedisRequest(connection.get(), redisIndex.get(), keyList));
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("file {} had been split to {} redis batch request, each batch size is {}", request.getFile(), futureList.size(), batchSize);
                    }
                    Future.all(futureList)
                            .onSuccess(ar -> {
                                List<Response> rList = ar.list();
                                if (log.isDebugEnabled()) {
                                    log.debug("received {} success redis response.", rList.size());
                                }

                                rList.forEach(r -> resultReport.getLoadedSize().addAndGet(r.toInteger()));
                                request.getConnection().close();
                                resultReport.end();
                                log.info("statistics info is {}", resultReport);
                                ctx.json(resultReport);
                            }).onFailure(err -> {
                                log.error(err.getMessage(), err);
                                request.getConnection().close();
                            });


                });


            });
        } catch (NullPointerException ne) {
            ctx.fail(400, ne);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ctx.fail(500, e);
        }


    }

    /**
     * Load file, use batch mode, batch size is 10,000, only support set data structure,
     * LOG result report include statistics information.
     *
     * @param originVertxContext the context of the original WebVertx
     * @param request            the file handler request
     * @param fn                 the function, accept line and return key which store in redis set structure.
     */
    public static void batchLoadFile(OriginWebVertxContext originVertxContext,
                                     LoadFileRequest request,
                                     Function<String, String> fn) {
        try {
            Optional<String> file = Optional.of(request.getFile());
            Optional<String> redisIndex = Optional.of(request.getIndex());
            Optional<RedisConnection> connection = Optional.of(request.getConnection());
            ResultReport resultReport = new ResultReport().start();
            if (log.isDebugEnabled()) {
                log.debug("receive a request {} that load file to redis.", request);
            }
            Future<AsyncFile> asyncFileFuture = originVertxContext.getFs().open(file.get(), new OpenOptions());
            List<Future<Response>> futureList = new ArrayList<>();
            AsyncResultHandler.handleFuture(asyncFileFuture, asyncFile -> {

                AtomicInteger batchSize = new AtomicInteger(10000);
                AtomicInteger cursor = new AtomicInteger(0);
                List<String> keyList = new ArrayList<>(batchSize.get());
                RecordParser recordParser = createRecordParserForBatch(request, fn, cursor, batchSize, keyList, futureList, resultReport);

                asyncFile.handler(recordParser).endHandler(v -> {

                    asyncFile.close();
                    if (!keyList.isEmpty()) {
                        futureList.add(createBatchRedisRequest(connection.get(), redisIndex.get(), keyList));
                    }
                    Future.all(futureList)
                            .onSuccess(ar -> {
                                List<Response> rList = ar.list();
                                rList.forEach(r -> {
                                    resultReport.getLoadedSize().addAndGet(r.toInteger());
                                });
                                request.getConnection().close();
                                resultReport.end();
                                log.info("statistics info is {}", resultReport);
                            }).onFailure(err -> {
                                log.error(err.getMessage(), err);
                                request.getConnection().close();
                            });


                });


            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


    }

    private static RecordParser createRecordParserForBatch(LoadFileRequest request,
                                                           Function<String, String> fn,
                                                           AtomicInteger cursor,
                                                           AtomicInteger batchSize,
                                                           List<String> keyList,
                                                           List<Future<Response>> futureList,
                                                           ResultReport resultReport) {
        return RecordParser.newDelimited(request.getDelimiter(), bufferLine -> {
            String line = bufferLine.toString(request.getEncode());
            if (log.isDebugEnabled()) {
                log.debug("line: {}", line);
            }
            try {
                String key = fn.apply(line);
                keyList.add(key.trim()
                        .replaceAll("\r", "")
                        .replaceAll("\n", ""));
                if (cursor.incrementAndGet() > batchSize.get()) {
                    futureList.add(createBatchRedisRequest(request.getConnection(), request.getIndex(), keyList));
                    cursor.set(0);
                    keyList.clear();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                resultReport.getErrorSize().incrementAndGet();
            }
            resultReport.getTotalSize().incrementAndGet();

        });
    }


    private static Future<Response> createBatchRedisRequest(RedisConnection connection, String redisIndex, List<String> keyList) {
        Request redisRequest = Request.cmd(Command.SADD)
                .arg(redisIndex);
        keyList.forEach(redisRequest::arg);

        return connection.send(redisRequest);
    }


    private static RecordParser createRecordParser(LoadFileRequest request, Function<String, Request> fn, ResultReport resultReport) {
        Optional<RedisConnection> connection = Optional.of(request.getConnection());

        return RecordParser.newDelimited(request.getDelimiter(), bufferLine -> {

            String line = bufferLine.toString(request.getEncode());
            if (log.isDebugEnabled()) {
                log.debug("line: {}", line);
            }

            if (request.isIgnoreFirstLine()) {
                if (resultReport.getTotalSize().get() > 0) {
                    try {
                        Request redisRequest = fn.apply(line);
                        if (redisRequest != null) {
                            connection.get().send(redisRequest)
                                    .onSuccess(ar -> resultReport.getLoadedSize().incrementAndGet())
                                    .onFailure(e -> resultReport.getErrorSize().incrementAndGet());
                        }

                    } catch (Exception e) {
                        log.error("handle line failed, caused by {}", e.getMessage(), e);
                        resultReport.getErrorSize().incrementAndGet();
                    }

                }
            } else {
                try {
                    Request redisRequest = fn.apply(line);
                    if (redisRequest != null) {
                        connection.get().send(redisRequest)
                                .onSuccess(ar -> resultReport.getLoadedSize().incrementAndGet())
                                .onFailure(e -> resultReport.getErrorSize().incrementAndGet());
                    }
                } catch (Exception e) {
                    log.error("handle line failed, caused by {}", e.getMessage(), e);
                    resultReport.getErrorSize().incrementAndGet();
                }
            }

            resultReport.getTotalSize().incrementAndGet();

        });
    }


}
