package com.kevin.sample.data.task;

import com.origin.starter.app.domain.OriginAppConfig;
import com.origin.starter.app.domain.OriginAppVertxContext;
import com.origin.starter.app.handler.AsyncResultHandler;
import com.origin.starter.app.spi.OriginTask;
import io.vertx.core.Future;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DataGenerator implements OriginTask {


    @Override
    public void run(OriginAppVertxContext originAppVertxContext, OriginAppConfig originAppConfig) {

        /**
         * simplify example
         */
        String file = originAppConfig.getAppConfig().getString("demo-file");
        Future<AsyncFile> asyncFileFuture = originAppVertxContext.getFs().open(file, new OpenOptions());
        LocalDateTime startedTime = LocalDateTime.now();
        AtomicInteger count = new AtomicInteger();
        AsyncResultHandler.handleFuture(asyncFileFuture, asyncFile -> {
            RecordParser recordParser = RecordParser.newDelimited("\n", bufferLine -> {
                String number = bufferLine.toString("UTF-8");
                log.info(number);
                originAppConfig.getEventBus().publish("data", number);
                count.incrementAndGet();
            });
            asyncFile.handler(recordParser).endHandler(v -> {
                asyncFile.close();

                log.info("cost {}s to handle {} data.", Duration.between(startedTime, LocalDateTime.now()).getSeconds(), count);
            });
        });
    }
}
