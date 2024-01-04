package com.origin.framework.file.domain;

import com.origin.framework.file.constants.Constants;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.redis.client.Response;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WriteFileWithRedisRequest {
    private String file;
    private String destFile;
    private String delimiter;
    private String encode;
    private boolean ignoreFirstLine;

    private List<Future<Response>> futures;
    private Buffer buffer;

    public WriteFileWithRedisRequest(String file, String destFile) {
        this.file = file;
        this.delimiter = Constants.DELIMITER;
        this.encode = Constants.ENCODE;
        this.destFile = destFile;
        this.ignoreFirstLine = true;
        this.futures = new ArrayList<>();
        this.buffer = Buffer.buffer();
    }

    public WriteFileWithRedisRequest withEncode(String encode) {
        this.encode = encode;
        return this;
    }

    public WriteFileWithRedisRequest withDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public WriteFileWithRedisRequest includeFirstLine() {
        this.ignoreFirstLine = false;
        return this;
    }

}
