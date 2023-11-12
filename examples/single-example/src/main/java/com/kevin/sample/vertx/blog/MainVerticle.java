package com.kevin.sample.vertx.blog;

import com.origin.starter.web.OriginWebApplication;
import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {
    public static void main(String[] args) {
        OriginWebApplication.runAsSingle(MainVerticle.class);
    }
}
