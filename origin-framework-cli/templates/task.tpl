package {{ .Group}}.{{ .ArtifactId}}.task;

import com.origin.framework.core.bean.OriginVertxContext;
import com.origin.framework.core.bean.OriginConfig;
import com.origin.framework.spi.OriginTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SampleTask implements OriginTask {

    @Override
    public void run(OriginVertxContext originAppVertxContext, OriginConfig originAppConfig) {
        // TODO..
    }

}