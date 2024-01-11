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
import io.vertx.redis.client.Request;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
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

    }

    /**
     * Load file, return the result report include statistics information to http response.
     *
     * @param originVertxContext the context of the original WebVertx
     * @param request            the file handler request
     * @param fn                 the function, accept line and return request, if you want to filter line, just return null.
     */
    public static void loadFile(OriginWebVertxContext originVertxContext,
                                LoadFileRequest request,
                                Function<String, Request> fn) {
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

    }


    private static RecordParser createRecordParser(LoadFileRequest request, Function<String, Request> fn, ResultReport resultReport) {
        return RecordParser.newDelimited(request.getDelimiter(), bufferLine -> {
            String line = bufferLine.toString(request.getEncode());
            if (log.isDebugEnabled()) {
                log.debug("line: {}", line);
            }
            if (request.isIgnoreFirstLine() && resultReport.getTotalSize().get() > 0) {
                try {
                    Request redisRequest = fn.apply(line);
                    if (redisRequest != null) {
                        request.getConnection().send(fn.apply(line))
                                .onSuccess(ar -> resultReport.getLoadedSize().incrementAndGet())
                                .onFailure(e -> resultReport.getErrorSize().incrementAndGet());
                    }

                } catch (Exception e) {
                    log.error("handle line failed, caused by {}", e.getMessage(), e);
                    resultReport.getErrorSize().incrementAndGet();
                }

            } else {
                try {
                    request.getConnection().send(fn.apply(line))
                            .onSuccess(ar -> resultReport.getLoadedSize().incrementAndGet())
                            .onFailure(e -> resultReport.getErrorSize().incrementAndGet());
                } catch (Exception e) {
                    log.error("handle line failed, caused by {}", e.getMessage(), e);
                    resultReport.getErrorSize().incrementAndGet();
                }
            }

            resultReport.getTotalSize().incrementAndGet();

        });
    }

}
