package com.origin.framework.file.domain.request;

import lombok.Data;

import static com.origin.framework.file.constants.Constants.DELIMITER;
import static com.origin.framework.file.constants.Constants.ENCODE;

/**
 * Basic file request, that contains file, line delimiter, encode and parse first line or not.
 * default value of the fields is:
 * delimiter: "\n"
 * encode: "UTF-8"
 * ignoreFirstLine: true
 *
 * @author Kevin Xu
 */
@Data
public class BasicFileRequest {
    private String file;
    private String delimiter;
    private String encode;
    private boolean ignoreFirstLine;

    public BasicFileRequest(String file) {
        this.delimiter = DELIMITER;
        this.encode = ENCODE;
        this.ignoreFirstLine = true;
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
