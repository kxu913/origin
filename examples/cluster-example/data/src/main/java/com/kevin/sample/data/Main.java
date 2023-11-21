package com.kevin.sample.data;


import com.origin.starter.app.OriginAppApplication;
import io.vertx.core.AbstractVerticle;

public class Main extends AbstractVerticle {
    public static void main(String[] args) {
        OriginAppApplication.runAsSingle(Main.class);
    }
}
