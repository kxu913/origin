package com.origin.starter.app.domain;

import io.vertx.core.Vertx;
import lombok.Data;
import io.vertx.core.file.FileSystem;


@Data
public class OriginAppVertxContext {
    private Vertx vertx;
    private FileSystem fs;

    public OriginAppVertxContext fromVertx(Vertx vertx) {
        this.fs = vertx.fileSystem();
        this.vertx = vertx;
        return this;
    }

}
