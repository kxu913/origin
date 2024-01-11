package com.origin.framework.file.domain.request;

import io.vertx.core.buffer.Buffer;
import lombok.Getter;

/**
 * Write file request extends BasicFileRequest, NEED a destFile.
 *
 * @author Kevin Xu
 * @see BasicFileRequest
 */
@Getter
public class WriteFileRequest extends BasicFileRequest {
    private final Buffer buffer;
    private String destFile;

    public WriteFileRequest(String file) {
        super(file);
        this.buffer = Buffer.buffer();
    }

    public WriteFileRequest withDestFile(String destFile) {
        this.destFile = destFile;
        return this;
    }

    public WriteFileRequest withEncode(String encode) {
        super.withEncode(encode);
        return this;
    }

    public WriteFileRequest withDelimiter(String delimiter) {
        super.withDelimiter(delimiter);
        return this;
    }

    public WriteFileRequest includeFirstLine() {
        super.includeFirstLine();
        return this;
    }

}
