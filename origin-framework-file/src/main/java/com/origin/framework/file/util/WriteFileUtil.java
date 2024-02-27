package com.origin.framework.file.util;


import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.core.handler.AsyncResultHandler;
import com.origin.framework.file.domain.request.WriteFileRequest;
import com.origin.framework.file.domain.response.ComposeResponse;
import com.origin.framework.file.domain.result.ResultReport;
import io.vertx.core.Future;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * it's a util class that used to write file from another file using some filter logic of line.
 *
 * @author Kevin Xu
 */
@Slf4j
public class WriteFileUtil {

    /**
     * Read from the specified file with Redis, return the result report include statistics information to http response.
     *
     * @param originVertxContext the context of the original WebVertx
     * @param ctx                the routing context
     * @param request            the file handler request
     * @param fn                 the consumer object of ComposeRequest, be sure that fn doesn't contain any async function.
     */
    public static void writeFile(OriginWebVertxContext originVertxContext,
                                 RoutingContext ctx,
                                 WriteFileRequest request,
                                 Consumer<ComposeResponse> fn) {
        Optional<String> file = Optional.of(request.getFile());
        Optional<String> destFile = Optional.of(request.getDestFile());
        ResultReport resultReport = new ResultReport().start();
        if (log.isDebugEnabled()) {
            log.debug("receive a request {} that use to retrieve data from one file and write to another.", request);
        }
        Future<AsyncFile> asyncFileFuture = originVertxContext.getFs().open(file.get(), new OpenOptions());
        AsyncResultHandler.handleFuture(asyncFileFuture, ctx, asyncFile -> {

            asyncFile.handler(createRecordParser(request, fn, resultReport)).endHandler(v -> {
                asyncFile.close();
                originVertxContext.getFs().writeFile(destFile.get(), request.getBuffer(), x -> {
                    ResultReport report = resultReport.additionalInfo("destFile", destFile.get()).end();
                    log.info("statistics info is {}", report);
                    if (!ctx.response().ended()) {
                        ctx.json(report);
                    }
                });

            });


        });

    }

    /**
     * Read from the specified file with Redis, return the result report include statistics information to http response.
     *
     * @param originVertxContext the context of the original WebVertx
     * @param request            the file handler request
     * @param fn                 the consumer object of ComposeRequest, be sure that fn doesn't contain any async function.
     */
    public static void writeFile(OriginWebVertxContext originVertxContext,
                                 WriteFileRequest request,
                                 Consumer<ComposeResponse> fn) {
        Optional<String> file = Optional.of(request.getFile());
        Optional<String> destFile = Optional.of(request.getDestFile());
        if (log.isDebugEnabled()) {
            log.debug("receive a request {} that use to retrieve data from one file and write to another.", request);
        }
        ResultReport resultReport = new ResultReport().start();
        Future<AsyncFile> asyncFileFuture = originVertxContext.getFs().open(file.get(), new OpenOptions());
        AsyncResultHandler.handleFuture(asyncFileFuture, asyncFile -> {
            asyncFile.handler(createRecordParser(request, fn, resultReport)).endHandler(v -> {
                asyncFile.close();
                originVertxContext.getFs().writeFile(destFile.get(), request.getBuffer(), x -> {
                    ResultReport report = resultReport.additionalInfo("destFile", destFile.get()).end();
                    log.info("statistics info is {}", report);

                });

            });

        });

    }

    private static RecordParser createRecordParser(WriteFileRequest request, Consumer<ComposeResponse> fn, ResultReport resultReport) {
        return RecordParser.newDelimited(request.getDelimiter(), bufferLine -> {
            String line = bufferLine.toString(request.getEncode());
            if (log.isDebugEnabled()) {
                log.debug("line: {}", line);
            }
            if (request.isIgnoreFirstLine()) {
                if (resultReport.getTotalSize().get() > 0) {
                    try {
                        fn.accept(new ComposeResponse(line).withResultReport(resultReport).withBuffer(request.getBuffer()));
                    } catch (Exception e) {
                        log.error("handle line failed, caused by {}", e.getMessage(), e);
                        resultReport.getErrorSize().incrementAndGet();
                    }
                }


            } else {
                try {
                    fn.accept(new ComposeResponse(line).withResultReport(resultReport).withBuffer(request.getBuffer()));
                } catch (Exception e) {
                    log.error("handle line failed, caused by {}", e.getMessage(), e);
                    resultReport.getErrorSize().incrementAndGet();
                }
            }

            resultReport.getTotalSize().incrementAndGet();

        });
    }

}
