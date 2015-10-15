# -*- coding: utf-8 -*-

import logging
import os
from tutorial import TutorialService # 导入thrift生成的service
from bfd.harpc import server         # 导入harpc 的server
from bfd.harpc.common import config  # 导入harpc 的配置库
 
# 设置日志级别，格式，以及文件路径
logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                    filename='./logs/serverdemo.log',
                    filemode='w')

# 定义thrift hander,定义echo 函数， 此函数名必须和thrift定义文件的函数名相同
class EchoServiceHandler:
    def __init__(self):
        pass
    def echo(self, msg):
        return msg

#定义回调函数， harpc 采用的是多进程方式，此函数在创建进程的时候会进行调用， 具体可以参考thrift python 多进程server
def callback():
    filename = "./logs/serverdemo.log_%s" % os.getgid()
    logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                    filename=filename,
                    filemode='w')
if __name__ == '__main__':
    # setting config
    #conf = config.Config("./etc/demo_server.conf")
    conf = config.Config() # 创建配置文件类
    conf.set("server", "service", "python_test$EchoService"); #设置服务名，此服务名会在zk上注册
    conf.set("server", "port", "9095") #设置server 的端口
    conf.set("server", "zk_connect_str", "172.18.1.22:2181") #设置zk的连接地址
    conf.set("server", "auth_user", "test") # 设置zk 的授权用户名
    conf.set("server", "auth_password", "test") # 设置zk 的 授权密码
    conf.set("server", "monitor", "True") # 设置是否监控server
    # TutorialService.Processor thrift生成的Processor， EchoServiceHandler 上面定义的handler， 此处和原生的thrift相同
    server_demo = server.GeventProcessPoolThriftServer(TutorialService.Processor, EchoServiceHandler(), conf)
    server_demo.set_post_fork_callback(callback) #设置回调函数
    server_demo.start() #启动服务
