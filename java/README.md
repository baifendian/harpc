### Java版本依赖

* 选择最新的harpc的稳定版本，参见Tags
* 推荐使用maven工程添加相关依赖
* pom中添加harpc依赖，直接在pom中添加如下依赖（已上传至中央仓库）
```
<dependency>
  <groupId>com.baifendian</groupId>
  <artifactId>harpc</artifactId>
  <version>1.2</version>
</dependency>
```

### 使用步骤

(1) thrift 文件定义

```
# file: demo.thrift
namespace java com.bfd.harpc.demo.gen
service EchoService {
    string echo(1: string msg);
}
```


(2) thrift --gen java demo.thrift
    
将生成的gen-java文件夹下的文件拷贝到maven工程的src/main/java目录下	
注意： Thrift程序的版本建议使用0.9.2及以上

(3) 项目目录结构

```
src/main/java
├── com
│   ├── bfd
│          ├── harpc
│   	      		├── demo                                         
│   	      	   		├── ServerDemo.java		        #服务端
│   	      	   		├── ClientDemo.java		        #客户端
│   	      	   		├── EchoServiceImpl.java          #接口实现类
│   	      	   		├── gen					        #thrift生成文件
│   	      	   			├── EchoService.java
src/main/resources
├── server.properties                                   #server配置文件
├── client.properties 						          #client配置文件
pom.xml
```

(4) Server端配置

```
#file:server.properties
#zookeeper连接字符串
registry.connectstr = 172.18.1.22:2181,172.18.1.23:2181,172.18.1.24:2181
#授权字符串，格式为：用户名:密码
registry.auth = admin:admin123
#zookeeper会话超时时间，单位ms
registry.timeout = 3000

#服务名(全称)：命名空间$服务名简称
server.service = com.bfd.harpc.demo$EchoService
#服务端口
server.port = 19090
#服务名
server.name = harpc-demo-server
#服务负责人
server.owner = dongsheng.fan@baifendian.com
#是否发送统计信息到zk
server.monitor = true
#发送的时间间隔，单位为s
server.interval = 60
#......
```

(5) Server代码示例

```java
String[] configs = new String[] { "classpath:server.properties"  };  // 配置文件路径
EchoServiceImpl impl = new EchoServiceImpl();

try {
    Server server = new Server(configs, impl);
	server.start(); // 启动服务，非阻塞

	// 阻塞主线程
	synchronized (ServerDemo.class) {
		while (running) {
			try {
				ServerDemo.class.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
} catch (Exception e) {
	e.printStackTrace();
}

```

(6) Client端配置

```
#file:client.properties
#zookeeper连接字符串
registry.connectstr = 172.18.1.22:2181,172.18.1.23:2181,172.18.1.24:2181
#zookeeper会话超时时间，单位ms
registry.timeout = 3000
#直连到不同的server,多个server以分号隔开（若配置address，则使用直连方式）
#client.address=172.18.1.22:19090;172.18.1.23:19090

#服务名
client.name = harpc-demo-client
#服务负责人
client.owner = dongsheng.fan@baifendian.com
#服务名(全称)：命名空间$服务名简称
client.service = com.bfd.harpc.demo$EchoService
#thrift生成文件的Iface接口
client.iface = com.bfd.harpc.demo.gen.EchoService$Iface
#client到server的超时时间，单位为ms
client.timeout = 10000
#重试次数
client.retry = 1
#......
```

(7) Client代码示例

``` java
String[] configs = new String[] { "classpath:client.properties" };

try {
    Client<Iface> client = new Client<Iface>(configs);
	// 注意:代理内部已经使用连接池，所以这里只需要创建一个实例，多线程共享
        // 特殊情况下，可以允许创建多个实例，但严禁每次调用前都创建一个实例
	Iface echoIface = client.createProxy();

	for (int i = 0; i < 1000; i++) {
		try {
			System.out.println(echoIface.echo("world"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
} catch (Exception e) {
	e.printStackTrace();
}
```
