package com.origin.framework.file.domain;

import io.vertx.core.buffer.Buffer;
import io.vertx.redis.client.RedisConnection;
import lombok.Data;

@Data
public class ComposeRequest {
    private String line;
    private RedisConnection connection;
    private ResultReport resultReport;
    private Buffer buffer;

    public ComposeRequest(String line) {
        this.line = line;
    }

    public ComposeRequest withConnection(RedisConnection connection) {
        this.connection = connection;
        return this;
    }

    public ComposeRequest withResultReport(ResultReport resultReport) {
        this.resultReport = resultReport;
        return this;
    }

    public ComposeRequest withBuffer(Buffer buffer) {
        if (buffer != null) {
            this.buffer = buffer;
        }
        return this;
    }

}
