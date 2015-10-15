### 下载安装
* 简要说明
  c++ 目前不依赖zk，client端通过直连方式，稳定性高，经过了线上两年考验

* git clone https://github.com/baifendian/harpc.git
 

* 编译工具
    cmake > 3.01

* 版本依赖  
    boost>=1.55
    g++ >= 4.8.1
    thrift>=0.9 (建议0.92）

### 使用步骤
(1) thrift 文件定义
```
#file  echo.thrift
namespace cpp bfd.harpc.demo
service EchoService {
   string echo(1:string msg)
}
```

(2)thrift --gen cpp echo.thrift

    将生成的gen-cpp文件夹下的文件拷贝到demo/thrift 中
    注意： Thrift程序的版本建议使用0.9.2及以上

(3) 项目目录结构
```
├── cmake       // 相关的 cmake 文件
│   ├── Tools.cmake
│   ├── demo_client.cmake
│   ├── demo_service.cmake
│   └── start.cmake
├── demo_client // client 端的示例代码
│   ├── conf
│   │   └── conf.xml    // 初始化 harpc 的配置文件，用户需要注意
│   └── cpp
│       └── src
│           └── main.cpp
├── demo_service        // server 端的示例代码
│   └── cpp
│       └── src
│           ├── DemoService.h
│           └── main.cpp
├── harpc       // 高可用 RPC 的内核
│   ├── ThriftClientPool.h      // Thrift 连接的一个封装，核心 class
│   ├── config.h        // 配置解析
│   └── myconnect.h     // 对一个连接的封装
├── release
│   └── CMakeLists.txt  // CMakeList 文件，进入到该目录，运行 cmake .; make {target} 会执行相应的命令
└── thrift      // thrift IDL 自动生成的文件
    ├── EchoService.cpp
    ├── EchoService.h
    ├── EchoService_server.skeleton.cpp
    ├── echo_constants.cpp
    ├── echo_constants.h
    ├── echo_types.cpp
    └── echo_types.h
```

(4) Server代码示例
```
// 创建hander类
class DemoService : virtual public bfd::harpc::demo::EchoServiceIf {
public:
    void echo(std::string &_return, const std::string &msg) {
        _return = "Hello, you are client";
    }
}; 

// 原生thrift server 加载
boost::shared_ptr<DemoService> handler(new DemoService());
boost::shared_ptr < TProcessor> processor(new EchoServiceProcessor(handler));
boost::shared_ptr<TServerTransport> serverTransport(new TServerSocket(port));
boost::shared_ptr<TProtocolFactory> protocolFactory(new TBinaryProtocolFactory());
boost::shared_ptr<TTransportFactory> transportFactory(new TBufferedTransportFactory());
TSimpleServer server(processor, serverTransport, transportFactory, protocolFactory);

// 服务启动
server.serve();

```

(5) Client端配置
```
<configuration>
        <thrift>
                <!– 所有server的ip和port-->
                <connect>127.0.0.1:11212,127.0.0.1:11213</connect>
                <connTimeOut>500</connTimeOut>
                <sendTimeOut>400</sendTimeOut>
                <recvTimeOut>400</recvTimeOut>
        </thrift>
        <threshold>
                <!– 针对每个server端的最大连接池大小 -->
                <maxPoolSize>50</maxPoolSize>
                <!– 针对每个server端的初始化连接池大小-->
                <initPoolSize>10</initPoolSize>
                <!—检查连接是否有效的间隔时间-->
                <checkInterval>15</checkInterval>
        </threshold>
</configuration>
```

(6) Client代码示例
```
std::string confPath = "../conf/conf.xml";

//bfd::harpc::demo::EchoServiceClient 是 thrift 生成的 client 接口
ThriftClientPool<bfd::harpc::demo::EchoServiceClient> * ms_client_pool =
            new ThriftClientPool<bfd::harpc::demo::EchoServiceClient>(confPath);
 
//函数代理
ms_client_pool->invoke([&](bfd::harpc::demo::EchoServiceClient *clintPtr) {
        clintPtr->echo(result, "Hello, service.");
    });

```
