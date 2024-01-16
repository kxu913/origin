package com.origin.framework.file.domain.request;

import io.vertx.redis.client.RedisConnection;
import lombok.Getter;

/**
 * Load file request extends BasicFileRequest, NEED a redis connection.
 *
 * @author Kevin Xu
 * @see BasicFileRequest
 */
@Getter
public class LoadFileRequest extends BasicFileRequest {
    private RedisConnection connection;
    private String index;


    public LoadFileRequest(String file) {
        super(file);
    }


    public LoadFileRequest withConnection(RedisConnection connection) {
        this.connection = connection;
        return this;
    }

    public LoadFileRequest withEncode(String encode) {
        super.withEncode(encode);
        return this;
    }

    public LoadFileRequest withDelimiter(String delimiter) {
        super.withDelimiter(delimiter);
        return this;
    }

    public LoadFileRequest includeFirstLine() {
        super.includeFirstLine();
        return this;
    }

    public LoadFileRequest withIndex(String index) {
        this.index = index;
        return this;
    }

}
