## Origin Framework
### 介绍
它是一个基于[Vert.x](https://vertx.io/)构建出来的一个框架，旨在简化使用Vert.x的使用。选择Vert.x，因为它的reactive模式，可以让应用更好的利用容器资源，它支持以集群模式或者单机模式运行应用，支持应用目前包括：
- Web application
- Standard application

配置大于代码，需要在代码中使用正确的配置来获取对应的类实例，使用spi模式注入基础类`com.origin.starter.web.spi.OriginRouter`或者`com.origin.starter.app.spi.OriginTask`

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

- 基础类使用spi模式，需要在`META/INF`下面创建对应的实现类，将实现类添加到service文件。
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
- Web工程，添加`com.originframework.web.spi.OriginTask`，代码中实现类的`route()`方法。
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
- 启动集群实例。集群依赖于zookeeper，需要有一个zookeeper集群，模式在同一个znode下面，可以互相通信，你也可以添加一个`zookeeper.json`到工程，指定不同的znode。
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
  

