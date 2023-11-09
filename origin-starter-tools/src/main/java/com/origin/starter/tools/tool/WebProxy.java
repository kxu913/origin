package com.origin.starter.tools.tool;

import com.origin.starter.tools.core.OriginVertxContext;
import com.origin.starter.tools.spi.OriginTool;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.proxy.handler.ProxyHandler;
import io.vertx.httpproxy.HttpProxy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebProxy implements OriginTool {
    private OriginVertxContext context;
    private JsonObject config;


    @Override
    public String getKey() {
        return "web-proxy";
    }

    @Override
    public void setOriginVertxContent(OriginVertxContext context) {
        this.context = context;

    }

    @Override
    public void setOriginConfig(JsonObject config) {
        this.config = config;

    }

    @Override
    public void init() {
        HttpClient proxyClient = context.getVertx().createHttpClient();
        JsonArray proxyList = this.config.getJsonArray("config");
        proxyList.forEach(proxy -> {
            JsonObject proxyConfig = new JsonObject(proxy.toString());
            JsonObject target = proxyConfig.getJsonObject("target");

            HttpProxy httpProxy = HttpProxy.reverseProxy(proxyClient);
//            httpProxy.originSelector()
            log.info("init a proxy use {}", proxy);
            httpProxy.origin(target.getInteger("port"), target.getString("server"));
            String method = target.getString("method");
            log.info("{}", proxyConfig.getString("path"));
            if (method == null || method.isEmpty()) {
                context.getRouter()
                        .route(HttpMethod.GET, proxyConfig.getString("path")).handler(ProxyHandler.create(httpProxy));
            } else {

                context.getRouter()
                        .route(HttpMethod.valueOf(method), proxyConfig.getString("path")).handler(ProxyHandler.create(httpProxy));
            }

        });

    }
}
