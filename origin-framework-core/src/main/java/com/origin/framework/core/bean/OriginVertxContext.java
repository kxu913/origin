package com.origin.framework.core.bean;

import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.shareddata.SharedData;
import lombok.Data;


@Data
public class OriginVertxContext {
    protected Vertx vertx;
    protected FileSystem fs;
    protected SharedData sharedData;


    public OriginVertxContext fromVertx(Vertx vertx) {
        this.fs = vertx.fileSystem();
        this.vertx = vertx;
        this.sharedData = vertx.sharedData();
        return this;
    }

}
