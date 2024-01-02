package {{ .Group}}.{{ .ArtifactId}};

{{if .App}}import com.origin.starter.app.OriginAppApplication;
{{ else}}import com.origin.starter.web.OriginWebApplication;{{ end}}
import io.vertx.core.AbstractVerticle;

public class Main extends AbstractVerticle {
    public static void main(String[] args) {
    
    {{ if .App}}{{if .Cluster}}    OriginAppApplication.runAsCluster(Main.class);{{else}}    OriginAppApplication.runAsSingle(Main.class);{{ end}}
    {{else}}{{if .Cluster}}    OriginWebApplication.runAsCluster(Main.class);{{else}}    OriginWebApplication.runAsSingle(Main.class);{{ end}}{{ end}}
        
    }
}
