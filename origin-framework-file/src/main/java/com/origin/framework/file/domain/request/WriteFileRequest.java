package com.origin.framework.file.domain;

import io.vertx.core.buffer.Buffer;
import lombok.Getter;

@Getter
public class WriteFileRequest extends BasicFileRequest {
    private String destFile;


    private final Buffer buffer;

    public WriteFileRequest(String file) {
        super(file);
        this.buffer = Buffer.buffer();
    }

    public WriteFileRequest withDestFile(String destFile) {
        this.destFile = destFile;
        return this;
    }

}
