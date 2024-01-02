package {{ .Group}}.{{ .ArtifactId}}.router;

import com.origin.framework.core.bean.OriginWebVertxContext;
import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.spi.OriginRouter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SampleRouter implements OriginRouter {

    @Override
    public void router(OriginWebVertxContext originVertxContext, OriginConfig originConfig) {
        originVertxContext.getRouter().get("/demo")
                .handler(ctx -> {
                    // TODO..
                    ctx.end("hello origin.");

                });
        
    }

}