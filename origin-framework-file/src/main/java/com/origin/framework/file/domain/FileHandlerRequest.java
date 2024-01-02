package com.origin.framework.file.domain;

import com.origin.framework.file.constants.Constants;
import lombok.Data;

@Data
public class FileHandlerRequest {
    private String file;
    private String destFile;
    private String delimiter;
    private String encode;
    private boolean ignoreFirstLine;

    public FileHandlerRequest(String file) {
        this.file = file;
        this.delimiter = Constants.DELIMITER;
        this.encode = Constants.ENCODE;
        this.ignoreFirstLine = true;
    }

    public FileHandlerRequest withDestFile(String destFile) {
        this.destFile = destFile;
        return this;
    }

    public FileHandlerRequest withDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public FileHandlerRequest includeFirstLine() {
        this.ignoreFirstLine = false;
        return this;
    }

}
