### 下载安装
* git clone http://git.baifendian.com/dongsheng.fan/ha-rpc.git 

* cd ha-rpc/python    sudo python setup.py install

* 版本依赖  
    gevent>=1.0
    kazoo>=2.0
    thrift>=0.9

### 使用步骤
(1) thrift 文件定义
```
#file tutorial.thrift
namespace py tutorial
service TutorialService {
    string echo(1:string str)
}
```

(2)thrift --gen py tutorial.thrift

注意： Thrift程序的版本建议使用0.9.2及以上

(3) 项目目录结构
```
demo
├── client.py                                            #客户端
├── direct_cliet.py                                      #客户端直连
├── etc
│   ├── client.conf                                      
│   ├── direct_client.conf
│   └── server.conf
├── gen-py                                               #thrift生成的代码
│   ├── __init__.py
│   └── tutorial
│       ├── constants.py
│       ├── __init__.py
│       ├── __init__.pyc
│       ├── ttypes.py
│       ├── ttypes.pyc
│       ├── TutorialService.py
│       ├── TutorialService.pyc
│       └── TutorialService-remote
└── server.py                                            #服务端   
```

(4) Server端配置
```
[server]
#服务名(全称)：命名空间$服务名简称, client端请求server的标识 
service=python_test$EchoService
#server服务端口
port=9095
#zookeeper的连接字符串
zk_connect_str=172.18.1.22:2181,172.18.1.23:2181,172.18.1.23:2181
#server授权用户字符串 避免其他服务也注册此服务
auth_user=test
#server授权用户字符串 避免其他服务也注册此服务名
auth_password=test
#是否监控server的状态和请求数等信息， 默认值是False
monitor=True
#server服务的名称
name=EchoServiceServerDemo
#server 服务负责人 
owner=wenting.wang@baifendian.com
#server 开启的进程数 默认值10个
process_num=10
#server 每个进程中的协程数目，默认值100个
coroutines_num=100
......
```

(5) Server代码示例
```
#thrift 接口实现
class EchoServiceHandler:
    def echo(self, msg):
        return msg

#配置加载
conf = config.Config("./etc/server.conf")

#TutorialService.Processor thrift生成的Processor
server_demo = server.GeventProcessPoolThriftServer(TutorialService.Processor,
                                                   EchoServiceHandler(), conf)
#服务启动
server_demo.start()
```

(6) Client端配置
```
[client]
#是否使用zk，默认值是True，如果使用直连方式连接server，需要设置成False
use_zk=True
#直连到不同的server,多个server以分号隔开
direct_address=172.18.1.101:19999;172.18.1.102:19999
#zookeeper连接字符串
zk_connect_str=172.18.1.22:2181,172.18.1.23:2181,172.18.1.24:2181
#服务名(全称)：命名空间$服务名简称,client端请求server的标识
service=python_test$EchoService
#client服务的名称
name=EchoServiceClientDemo
#client服务负责人
owner=wenting.wang@baifendian.com
#client请求失败时，重试次数
retry=3
#client负载均衡策略，默认轮询
balance=bfd.harpc.loadbalancing_strategy.round_robin_strategy.RoundRobinStrategy
[loadbalancer] 
#心跳的interval时间，默认值10秒
heartbeat_interval=10
[connection_pool] 
#连接池大小
pool_size=10
......
```

(7) Client代码示例
```
#读取配置文件
conf = config.Config(“./etc/client.conf”)

#创建实例
 manager = client.Client(TutorialService.Client, conf)

#创建代理实例
 proxy_client = manager.create_proxy()

#接口调用
 proxy_client.echo("hello world!")

#关闭
 manager.close()
```
