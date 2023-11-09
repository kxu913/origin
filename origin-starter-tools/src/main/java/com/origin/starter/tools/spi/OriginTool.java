package com.origin.starter.tools.spi;

import com.origin.starter.tools.core.OriginVertxContext;
import io.vertx.core.json.JsonObject;

public interface OriginTool {
    public String getKey();

    public void setOriginVertxContent(OriginVertxContext context);

    public void setOriginConfig(JsonObject config);

    public void init();
}
