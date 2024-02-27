package com.origin.framework.file.util;


import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.core.handler.AsyncResultHandler;
import com.origin.framework.file.domain.request.BasicFileRequest;
import com.origin.framework.file.domain.result.ResultReport;
import io.vertx.core.Future;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * it's a util class that used to read file.
 *
 * @author Kevin Xu
 */
@Slf4j
public class ReadFileUtil {

    /**
     * Read file, return the result report include statistics information to http response.
     *
     * @param originVertxContext the context of the original WebVertx
     * @param request            the file handler request
     * @param fn                 the consumer object of ComposeRequest, be sure that fn doesn't contain any async function.
     * @param callback           the callback after file parse end.
     */
    public static void readFile(OriginWebVertxContext originVertxContext,
                                BasicFileRequest request,
                                BiConsumer<String, ResultReport> fn,
                                Runnable callback) {
        Optional<String> file = Optional.of(request.getFile());
        ResultReport resultReport = new ResultReport().start();
        if (log.isDebugEnabled()) {
            log.debug("receive a request {} that use to read file.", request);
        }
        Future<AsyncFile> asyncFileFuture = originVertxContext.getFs().open(file.get(), new OpenOptions());
        AsyncResultHandler.handleFuture(asyncFileFuture, asyncFile -> {

            asyncFile.handler(createRecordParser(request, fn, resultReport)).endHandler(v -> {
                asyncFile.close();
                log.info("statistics info is {}", resultReport);
                callback.run();


            });


        });

    }


    private static RecordParser createRecordParser(BasicFileRequest request, BiConsumer<String, ResultReport> fn, ResultReport resultReport) {
        return RecordParser.newDelimited(request.getDelimiter(), bufferLine -> {
            String line = bufferLine.toString(request.getEncode());
            if (log.isDebugEnabled()) {
                log.debug("line: {}", line);
            }
            if (request.isIgnoreFirstLine()) {
                if (resultReport.getTotalSize().get() > 0) {
                    try {
                        fn.accept(line, resultReport);
                    } catch (Exception e) {
                        log.error("handle line failed, caused by {}", e.getMessage(), e);
                        resultReport.getErrorSize().incrementAndGet();
                    }
                }
            } else {
                try {
                    fn.accept(line, resultReport);
                } catch (Exception e) {
                    log.error("handle line failed, caused by {}", e.getMessage(), e);
                    resultReport.getErrorSize().incrementAndGet();
                }
            }

            resultReport.getTotalSize().incrementAndGet();

        });
    }

}
