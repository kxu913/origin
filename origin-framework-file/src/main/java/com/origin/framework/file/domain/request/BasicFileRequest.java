package com.origin.framework.file.domain;

import lombok.Data;

@Data
public class BasicFileRequest {
    private String file;
    private String delimiter;
    private String encode;
    private boolean ignoreFirstLine;

    public BasicFileRequest(String file) {
        this.file = file;
    }

    public BasicFileRequest withEncode(String encode) {
        this.encode = encode;
        return this;
    }

    public BasicFileRequest withDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public BasicFileRequest includeFirstLine() {
        this.ignoreFirstLine = false;
        return this;
    }
}
