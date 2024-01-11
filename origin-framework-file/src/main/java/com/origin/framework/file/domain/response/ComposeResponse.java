package com.origin.framework.file.domain.response;

import com.origin.framework.file.domain.result.ResultReport;
import io.vertx.core.buffer.Buffer;
import io.vertx.redis.client.RedisConnection;
import lombok.Data;

@Data
public class ComposeResponse {
    private String line;
    private RedisConnection connection;
    private ResultReport resultReport;
    private Buffer buffer;

    public ComposeResponse(String line) {
        this.line = line;
    }

    public ComposeResponse withConnection(RedisConnection connection) {
        this.connection = connection;
        return this;
    }

    public ComposeResponse withResultReport(ResultReport resultReport) {
        this.resultReport = resultReport;
        return this;
    }

    public ComposeResponse withBuffer(Buffer buffer) {
        if (buffer != null) {
            this.buffer = buffer;
        }
        return this;
    }

}
