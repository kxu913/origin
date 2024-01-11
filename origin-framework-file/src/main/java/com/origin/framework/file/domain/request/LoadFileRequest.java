package com.origin.framework.file.domain;

import io.vertx.redis.client.RedisConnection;
import lombok.Getter;


@Getter
public class LoadFileRequest extends BasicFileRequest {
    private RedisConnection connection;


    public LoadFileRequest(String file) {
        super(file);
    }


    public LoadFileRequest withConnection(RedisConnection connection) {
        this.connection = connection;
        return this;
    }


}
