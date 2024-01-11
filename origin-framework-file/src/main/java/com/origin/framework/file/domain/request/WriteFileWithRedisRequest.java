package com.origin.framework.file.domain.request;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.redis.client.Response;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Write file request with Redis extends BasicFileRequest, NEED destFile.
 *
 * @author Kevin Xu
 * @see BasicFileRequest
 */
@Getter
public class WriteFileWithRedisRequest extends BasicFileRequest {

    private final List<Future<Response>> futures;
    private final Buffer buffer;
    private String destFile;

    public WriteFileWithRedisRequest(String file) {
        super(file);
        this.futures = new ArrayList<>();
        this.buffer = Buffer.buffer();
    }

    public WriteFileWithRedisRequest withDestFile(String destFile) {
        this.destFile = destFile;
        return this;
    }

    public WriteFileWithRedisRequest withEncode(String encode) {
        super.withEncode(encode);
        return this;
    }

    public WriteFileWithRedisRequest withDelimiter(String delimiter) {
        super.withDelimiter(delimiter);
        return this;
    }

    public WriteFileWithRedisRequest includeFirstLine() {
        super.includeFirstLine();
        return this;
    }
}
