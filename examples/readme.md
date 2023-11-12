## `single-example`

启动一个web工程，提供一个从数据库获取数据的例子。

## `cluster-example`

### `data`

启动一个app工程，用来创建数据，定时发送到eventbus，

### `es-data`

启动一个web工程，用来消费`data`项目中生成的数据，并将数据写入到es索引中，并提供查询接口。

### `redis-data`

启动一个web工程，用来消费`data`项目中生成的数据，并将数据写入到redis中，并提供查询接口。

**需要确保`zookeeper.json`文件里面用的是同一个`rootPath`，否则会消费不了消息。**


