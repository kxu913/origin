## Origin Framework
*其他语言版本: [English](readme.md), [中文](readme_zh.md)*
### 介绍

它是一个基于[Vert.x](https://vertx.io/)
构建出来的一个框架，旨在简化使用Vert.x的使用。选择Vert.x，因为它的reactive模式，可以让应用更好的利用容器资源，它支持以集群模式或者单机模式运行应用，支持应用目前包括：

- Web application
- Standard application

配置大于代码，需要在代码中使用正确的配置来获取对应的类实例，使用spi模式注入基础类`com.origin.starter.web.spi.OriginRouter`
或者`com.origin.starter.app.spi.OriginTask`

### 性能测试
*测试接口，从数据库查询100条数据，包括一个长text字段，[测试代码](https://github.com/kxu913/comparison)*
- 数据库连接池设置为50。
- docker设置最大内存为512m。

**在并发数不高的情况下，springBoot的响应时间略快于origin，但是在并发数高的情况下，origin有明显的优势，在docker模式下，限定内存越低，origin优势更明显，限定为128m的情况下，springBoot会出现错误率。**

<table>
<tr>
<td rowspan="2">并发</td>
<td colspan="2">Springboot 本地运行模式</td>
<td colspan="2">Origin 本地运行模式</td>
<td colspan="2">Springboot Docker运行模式</td>
<td colspan="2">Origin Docker运行模式</td>
</tr>
<tr>
<td>平均响应时间(ms)</td>
<td>最大响应时间(ms)</td>
<td>平均响应时间(ms)</td>
<td>最大响应时间(ms)</td>
<td>平均响应时间(ms)</td>
<td>最大响应时间(ms)</td>
<td>平均响应时间(ms)</td>
<td>最大响应时间(ms)</td>
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

### 模块
- origin-starter-pom
pom工程，声明项目的依赖以及包括的模块。
- origin-starter-web
用于快速创建一个web工程。
- origin-starter-app
用于快速创建一个标准应用。

### 用法

目前没有push到maven仓库，需要在本地安装。

- clone工程到本地。
- 按需编译工程。
- 在个人的项目中按需引入依赖。
    - 启动一个web工程
      ```xml
      <dependency>
          <groupId>com.originframework</groupId>
          <artifactId>origin-starter-web</artifactId>
          <version>1.0-SNAPSHOT</version>
          <scope>compile</scope>
          <type>jar</type>
      </dependency>
      ```
    - 启动一个app工程
      ```xml
      <dependency>
          <groupId>com.originframework</groupId>
          <artifactId>origin-starter-app</artifactId>
          <version>1.0-SNAPSHOT</version>
          <scope>compile</scope>
          <type>jar</type>
      </dependency>
      ```

### 配置

- 基础类使用spi模式，需要在`META-INF`下面创建对应的实现类，将实现类添加到service文件。
  - Web工程，添加`com.originframework.web.spi.OriginRouter`，代码中实现类的`route()`方法。
    示例：

    ```java
    
    public class BlogRouter implements OriginRouter {
        @Override
        public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {
    
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

  - Standard工程，添加`com.originframework.web.spi.OriginTask`，代码中实现类的`run()`方法。
  示例：
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

- 配置优先代码，需要在添加配置文件，`conf/config.json`, 根据配置中声明的模块获取相应实例。
  完整配置

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

### 启动

- 启动单机实例。
  - 启动web工程。
    ```java
    public class Main extends AbstractVerticle {
        public static void main(String[] args) {
            OriginWebApplication.runAsSingle(Main.class);
        }
    }
    ```

  - 启动app项目。
    ```java
    public class Main extends AbstractVerticle {
        public static void main(String[] args) {
            OriginAppApplication.runAsSingle(Main.class);
        }
    }
    ```

- 启动集群实例。集群依赖于zookeeper，需要有一个zookeeper集群，模式在同一个znode下面，可以互相通信，你也可以添加一个`zookeeper.json`
到工程，指定不同的znode。
  - 启动web工程。
    ```java
    public class Main extends AbstractVerticle {
        public static void main(String[] args) {
            OriginWebApplication.runAsCluster(Main.class);
        }
    }
    ```

  - 启动app项目。
    ```java
    public class Main extends AbstractVerticle {
        public static void main(String[] args) {
            OriginAppApplication.runAsCluster(Main.class);
        }
    }
    ```

### 打包

- 打包成可运行的jar包，添加`maven-shade-plugin`插件，指定main函数，避免依赖包的java版本冲突，需要exclude掉版本声明文件。

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

- 利用native-image生成可执行文件，需要添加`native-image-maven-plugin`插件，同时添加一个`META-INF\native-image`
  目录下添加一个`native-image.properties`
  文件，声明jni，反射，proxy等文件的处理，以及打包设置，这些文件可以通过` -agentlib:native-image-agent`来生成。
    - 假设你已经生成了一个可执行的jar包，可以通过以下命令来生成这些配置文件。
      `java -agentlib:native-image-agent=config-output-dir=./src/main/resources/META-INF/native-image -cp .\target\config-1.0.0-SNAPSHOT-fat.jar com.kevin.sample.vertx.config.ConfigVerticle`
    - 编写`native-image.properties`文件，内容如下，由于项目中用logback替换掉了netty原生的log框架，所以需要添加额外的一些slf4j的配置。
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
    - 添加`native-image-maven-plugin`插件，运行`mvn clean package`就可以生成一个不依赖于jvm的可执行文件。
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

### 容器化

- `docker/Dokerfile`,通过源码编译成一个native-image打包的docker镜像，需要在项目中包括maven环境，可以将`.mvn`添加到项目中。
- `docker/Dokerfile.fat-jar`，通过可执行的jar包编译成一个native-image打包的docker镜像。
- `docker/Dokerfile.legacy-jar`，通过可执行的jar包编译成一个基于openjdk的docker镜像。

### 示例

[示例代码](./examples)包括启动一个简单的web工程和启动一套数据处理的app工程。