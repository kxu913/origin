package com.origin.framework.file.domain;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.redis.client.Response;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class WriteFileWithRedisRequest extends BasicFileRequest {

    private String destFile;


    private final List<Future<Response>> futures;
    private final Buffer buffer;

    public WriteFileWithRedisRequest(String file) {
        super(file);
        this.futures = new ArrayList<>();
        this.buffer = Buffer.buffer();
    }

    public WriteFileWithRedisRequest withDestFile(String destFile) {
        this.destFile = destFile;
        return this;
    }

}
