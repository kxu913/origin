package com.origin.framework.file.domain;

import com.origin.framework.file.constants.Constants;
import io.vertx.core.buffer.Buffer;
import lombok.Data;

@Data
public class WriteFileRequest {
    private String file;
    private String destFile;
    private String delimiter;
    private String encode;
    private boolean ignoreFirstLine;

    private Buffer buffer;

    public WriteFileRequest(String file, String destFile) {
        this.file = file;
        this.delimiter = Constants.DELIMITER;
        this.encode = Constants.ENCODE;
        this.destFile = destFile;
        this.ignoreFirstLine = true;
        this.buffer = Buffer.buffer();
    }

    public WriteFileRequest withEncode(String encode) {
        this.encode = encode;
        return this;
    }

    public WriteFileRequest withDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public WriteFileRequest includeFirstLine() {
        this.ignoreFirstLine = false;
        return this;
    }

}
