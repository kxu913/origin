## Origin Framework
*Read this in other languages: [English](readme.md), [中文](readme_zh.md)*
### Introduce

Origin is a framework base on [Vert.x](https://vertx.io/), it used to simplify the process to create a web/standard application. 

Select Vert.x due to its principle is reactive and container first.

It supports the following applications:
- Web application
- Standard application

Configure firstly, due to reactive methodology, the first thing is get configuration then initialize class, then the instance of class store in BeanFactory.

Use SPI to inject basic class.
- Web basic class, `com.origin.starter.web.spi.OriginRouter`
- Standard basic class, `com.origin.starter.app.spi.OriginTask`

Provide Single Patten and Cluster Patten.
- Single Patten, it's a separate instance.
- Cluster Patten, it depends on Zookeeper, and all instance on same znode share data and eventbus and so on.

### Performance Result
*Test case is get 100 data from database, and result row contains a long text, [Test Code](https://github.com/kxu913/comparison)*

*Test Setting*
- Database connection pool size is 50.
- Restrict memory is 512m for docker.

*Conclusion*

- Low concurrency, RT of Springboot will less than origin's RT.
- High concurrency, RT of origin will less than Springboot's RT.
- Docker, RT of origin will be less than Springboot's RT. In case of 128M memory, springboot application occurs OOM error sometimes. 

<table>
<tr>
<td rowspan="2">Test Cases</td>
<td colspan="2">Springboot Local</td>
<td colspan="2">Origin Local</td>
<td colspan="2">Springboot Docker</td>
<td colspan="2">Origin Docker</td>
</tr>
<tr>
<td>Avg(ms)</td>
<td>Max(ms)</td>
<td>Avg(ms)</td>
<td>Max(ms)</td>
<td>Avg(ms)</td>
<td>Max(ms)</td>
<td>Avg(ms)</td>
<td>Max(ms)</td>
</tr>
<tr>
<td>50Threads*10Loops</td>
<td>572</td>
<td>625</td>
<td>611</td>
<td>687</td>
<td>761</td>
<td>890</td>
<td>876</td>
<td>1226</td>
</tr>
<tr>
<td>100Threads*10Loops</td>
<td>1083</td>
<td>2134</td>
<td>805</td>
<td>1117</td>
<td>1645</td>
<td>3211</td>
<td>1290</td>
<td>2460</td>
</tr>
<tr>
<td>500Threads*1Loops</td>
<td>4652</td>
<td>6440</td>
<td>3051</td>
<td>3722</td>
<td>8023</td>
<td>10742</td>
<td>4944</td>
<td>6819</td>
</tr>
</table>

### Modules
- origin-starter-pom
pom project, declare dependencies and versions.
- origin-starter-web
Use to create a web application.
- origin-starter-app
Use to create a standard application.

### Usage

due to Origin doesn't push to center maven repository, so you need to clone this project and compile it by yourself.

- clone the project.
- compile the project.
- add the dependency to your project.
    - create a web project.
      ```xml
      <dependency>
          <groupId>com.originframework</groupId>
          <artifactId>origin-starter-web</artifactId>
          <version>2.0</version>
          <scope>compile</scope>
          <type>jar</type>
      </dependency>
      ```
    - create a standard project.
      ```xml
      <dependency>
          <groupId>com.originframework</groupId>
          <artifactId>origin-starter-app</artifactId>
          <version>2.0-SNAPSHOT</version>
          <scope>compile</scope>
          <type>jar</type>
      </dependency>
      ```

### Configuration

- create a spi file in `META-INF`, then add your service into the file.
    - Web application，add a spi file named `com.origin.framework.spi.OriginRouter` in `META-INF`, then create a class implement `com.origin.framework.spi.OriginRouter` and add apis in `route()` method.
      Example:

    ```java
    
    public class BlogRouter implements OriginRouter {
        @Override
        public void router(OriginWebVertxContext originVertxContext, OriginConfig originConfig) {
    
            originVertxContext.getRouter().get("/blog")
                    .handler(ctx -> {
                        SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
                        sqlClient.preparedQuery("select * from blog limit 10").execute()
                                .onComplete(ar -> {
                                    if (ar.succeeded()) {
                                        RowSet<Row> rowSet = ar.result();
                                        List<JsonObject> results = new ArrayList<>(rowSet.size());
                                        rowSet.forEach(row -> results.add(row.toJson()));
                                        ctx.json(results);
                                    } else {
                                        ctx.fail(500, ar.cause());
                                    }
                                    sqlClient.close();
                                })
                                .onFailure(err -> ctx.fail(500, err));
                    });
    
        }
    }
    ```
    - Standard application，add a spi file named `com.origin.framework.spi.OriginTask` in `META-INF`, then create a class implement `com.origin.framework.spi.OriginTask` and add business logic in `run()` method.
      Example:

    ```java
    public class DataGenerator implements OriginTask {
    
        @Override
        public void run(OriginAppVertxContext originAppVertxContext, OriginAppConfig originAppConfig) {
            originAppVertxContext.getVertx().setPeriodic(5000, t -> {
                originAppConfig.getEventBus().publish("data", "demo");
            });
        }
    }
    
    ```

- configuration first, create a configuration file named `conf/config.json`, and add child configuration in the file to initialize modules you required, such as `server`,`db`,`es`,`redis` and so on.
  Completed configuration.

```json
{
  "server": {
    "host": "127.0.0.1",
    "port": 8080
  },
  "db": {
    "port": 5432,
    "host": "127.0.0.1",
    "user": "postgres",
    "password": "postgres",
    "database": "origin",
    "pool": {
      "maxSize": 20
    }
  },
  "redis": {
    "endpoint": "redis://localhost:6379",
    "role ": "MASTER",
    "maxWaitingHandlers": 2048,
    "netClientOptions": {
      "TcpKeepAlive": true,
      "TcpNoDelay": true
    }
  },
  "es": {
    "host": "localhost",
    "port": 9200,
    "schema": "http",
    "data-type": "application/json"
  }
}
```

### Starter

- Start a single application.
  - start a web application.
    ```java
    public class Main extends AbstractVerticle {
        public static void main(String[] args) {
            OriginWebApplication.runAsSingle(Main.class);
        }
    }
    ```
  - Start a standard application.
  ```java
    public class Main extends AbstractVerticle {
        public static void main(String[] args) {
            OriginAppApplication.runAsSingle(Main.class);
        }
    }
    ```
- Start an application that register into zookeeper node, it depends on zookeeper, all instance share data, eventbus if they are under a same zookeeper znode, you can add a `zookeeper.json` into your project to specify znode.
  - Start a web application
    ```java
    public class Main extends AbstractVerticle {
        public static void main(String[] args) {
            OriginWebApplication.runAsCluster(Main.class);
        }
    }
    ```
  - Start a standard application.
    ```java
    public class Main extends AbstractVerticle {
        public static void main(String[] args) {
            OriginAppApplication.runAsCluster(Main.class);
        }
    }
    ```

### Package

- package an executable jar, add `maven-shade-plugin` into pom.xml, specify main class, to avoid conflict of java version, should exclude `SF`,`RSA`,`DSA` files.

```xml

<plugin>
    <artifactId>maven-shade-plugin</artifactId>
    <version>${maven-shade-plugin.version}</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <transformers>
                    <transformer
                            implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <manifestEntries>
                            <Main-Class>${main.verticle}</Main-Class>
                            <Main-Verticle>${main.verticle}</Main-Verticle>
                        </manifestEntries>
                    </transformer>
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                </transformers>
                <outputFile>${project.build.directory}/${project.artifactId}-${project.version}-fat.jar
                </outputFile>
                <filters>
                    <filter>
                        <artifact>*:*</artifact>
                        <excludes>
                            <exclude>META-INF/*.SF</exclude>
                            <exclude>META-INF/*.DSA</exclude>
                            <exclude>META-INF/*.RSA</exclude>
                        </excludes>
                    </filter>
                </filters>
            </configuration>
        </execution>
    </executions>
</plugin>
```

- create an executable file, add `native-image-maven-plugin` into pom.xml, it depends on `native-image` of `GraalVM`, so need add `native-image.properties` in `META-INF\native-image`
  folder, the file specify how to package and how to lint static files, and you can use argument ` -agentlib:native-image-agent` to generate jni,proxy files, below is steps.
  - generate static files.
    ```shell
    `java -agentlib:native-image-agent=config-output-dir=./src/main/resources/META-INF/native-image -cp .\target\config-1.0.0-SNAPSHOT-fat.jar com.kevin.sample.vertx.config.ConfigVerticle`
    ```
  - create `native-image.properties` file.
  ```json
  Args =\
  --report-unsupported-elements-at-runtime \
  --initialize-at-run-time=io.netty.handler.ssl \
  --initialize-at-build-time=org.slf4j.LoggerFactory,ch.qos.logback \
  --trace-class-initialization=org.slf4j.MDC \
  -H:ReflectionConfigurationResources=${.}/reflect-config.json \
  -H:JNIConfigurationResources=${.}/jni-config.json \
  -H:ResourceConfigurationResources=${.}/resource-config.json \
  -H:Class=com.kevin.sample.vertx.config.ConfigVerticle \
  -H:+PrintClassInitialization \
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  -H:Name=config \
  --initialize-at-run-time=\
  io.netty.handler.codec.compression.ZstdOptions
  ```
  - add `native-image-maven-plugin` plugin, run`mvn clean package`to generate an executable file.
    ```xml
    <plugin>
    <groupId>org.graalvm.nativeimage</groupId>
    <artifactId>native-image-maven-plugin</artifactId>
    <version>${graal.version}</version>
    <executions>
      <execution>
        <goals>
          <goal>native-image</goal>
        </goals>
        <phase>package</phase>
      </execution>
    </executions>
    </plugin>
    ```

### Containerize

- `docker/Dokerfile`, build a docker image by source codes, depends on maven wrapper.
- `docker/Dokerfile.fat-jar`, build a docker image by an executable jar file.
- `docker/Dokerfile.legacy-jar`, build a docker image by an executable jar file, base image is openjdk.

### Demo

[Examples](./examples)include an example to start a web application and an example to create data flow cluster base on eventbus. 