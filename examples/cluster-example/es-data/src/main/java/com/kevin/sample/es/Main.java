package com.kevin.sample.es;

import com.origin.starter.web.OriginWebApplication;
import io.vertx.core.AbstractVerticle;

public class Main extends AbstractVerticle {
    public static void main(String[] args) {
        OriginWebApplication.runAsCluster(Main.class);
    }
}
