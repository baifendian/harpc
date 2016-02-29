### 管理系统说明（使用前请务必仔细阅读本说明）

- 管理系统主要展示两个页面，service总览、server管理&client列表；
- 服务总览页面罗列出所有的服务信息，包括：服务名、server列表、client列表、操作；
- server管理页面：展示每个server的配置信息和支持查看统计图表；
- client管理页面：展示每个client的配置信息和支持查看统计图表;
- 服务管理需要支持权限管理，包括两类用户admin和guest，admin登陆需要密码，运行操作服务节点（添加和删除），guest则只能查看信息无法进行任何操作。
- 只有admin用户使用本系统手动添加的server信息才允许删除，server程序自行注册的server信息不允许删除；
- 管理系统内置缓存，会缓存整个ZK以减少查询ZK，减轻ZK负担，缓存的更新使用2个种方式，ZK监听器自动更新 or 用户手动/半自动触发式，考虑到ZK负担，默认关闭了ZK自动监听更新。您在使用管理系统事如果发现管理系统显示的数据有误，请先点击‘强制刷新缓存’按钮，再根据提示刷新页面可获取最新的ZK数据。

### 管理系统依赖

管理系统依赖环境：
python 2.7 ；django 1.8.X（1.9以上的django未测试兼容性，理论兼容） ；kazoo ；uwsgi ；mysql ；

请注意本文档所指的根目录为本文档所在目录。本文档所指源码包仅指根目录内源码。

### 安装必要python环境

1. 输入`python --version`查看python版本为2.7.X则不需要安装python，如没有安装python或python版本太旧。根据您的操作系统版本选择`yum install python 2.7` 或 `apt-get install python 2.7` 安装python 2.7
2. python环境安装后使用pip工具，执行命令`pip install django==1.8.6`安装django。
3. 执行命令`pip install kazoo`安装kazoo（python zk驱动）。
4. 执行命令`pip install uwsgi` 安装uwsgi（python web容器）。
5. 安装mysql-python驱动,下载对应的mysql驱动包https://pypi.python.org/pypi/MySQL-python,解压执行
    `python setup.py build`
    `python setup.py install`

### 部署方式

1. 下载源码包

2. 进入mysql数据库执行管理系统数据库创建命令
`CREATE DATABASE harpc_admin;`

3. 根据mysql配置信息，ZK信息配置源码包配置文件，具体配置方式参见配置详解

4. 进入源码包根目录执行命令，该命令用于初始化管理系统表结构，使用该命令是可根据提示创建admin账号,该账号为系统管理员账号,如果此处忘记设置管理员可删除数据库重新重复1~4步骤
`python manage.py syncdb`。
请注意如果此处您使用了1.9或更高版本的django，请使用
`python manage.py makemigrations`
`python manage.py migrate`命令，在1.9+的django版本中syncdb命令已经被废弃。

5. 在源码包根目录执行命令，进入系统DEBUG模式，如果启动成功则配置一切正常
`python manage.py runserver 0.0.0.0:8000`

6. 在系统DEBUG模式下登录http://localhost:8000/admin/ 该地址为系统后台管理位置，由django原生提供，这设置其他用户等信息,使用4步骤设置的超级管理员登录,在创建用户是设置用户权限中可搜索manage | task | Can guest tasks,此为游客权限,或者manage | task | Can admin tasks,此为管理员权限

7. 关闭DEBUG模式，在源码根目录创建uwsgi配置文件，修改源码包配置文件(参见配置详解)，修改根目录下etc目录内的harpc_admin.ini配置文件，修改方法参见uwsgi配置文件简单说明，进入根目录下bin目录执行start.sh脚本启动系统。

8. 访问 http://localhost:8000/ 登录系统，部署完成

### 配置详解

- 配置文件位于源码包根目录下/harpc_admin/settings.py文件
- 配置文件重要字段说明：

```python
#debug模式开关，使用debug命令启动管理系统时请务必使用True,使用uwsgi时请务必使用False关闭debug模式
DEBUG = True
#debug模式下请务必注释该条配置，使用uwsgi部署是请务必保留该条配置
STATIC_ROOT = os.path.join(BASE_DIR, 'static').replace('\\','/')
#zk host配置
ZK_HOSTS='172.18.1.22:2182'
#zk 根目录配置（放置service节点的目录称为根目录）
ZK_ROOT='/harpc'
#zk 超时时间
ZK_TIMEOUT=10.0
#zk servers节点名称（servers节点存放在该目录下），如无特殊情况，使用默认值即可
ZK_SERVERS='servers'
#zk clients节点名称（clients节点存放在该目录下），如无特殊情况，使用默认值即可
ZK_CLIENTS='clients'
#zk configs节点名称（configs节点存放在该目录下），如无特殊情况，使用默认值即可
ZK_CONFIGS='configs'
#zk statistics节点名称（statistics节点存放在该目录下），如无特殊情况，使用默认值即可
ZK_STATISTICS='statistics'
#管理系统在zk内注册的用户名(管理系统使用ZK内置权限管理策略，启动时会向ZK注册管理系统专用用户)
ZK_USERNAME='harpc_admin'
#管理系统在zk内注册的密码
ZK_PASSWORD='123456'
#管理系统日志查询字段配置，禁止修改此项，该项尚不支持动态修改
ZK_STATISTICS_SERIES=[{'name':'avgtime','unit':0},{'name':'mintime','unit':0},{'name':'maxtime','unit':0},{'name':'qps','unit':1},{'name':'success','unit':1},{'name':'failure','unit':1}]
#数据库配置
DATABASES = {
'default': {
'ENGINE': 'django.db.backends.mysql',
'NAME': 'harpc_admin',
'USER': 'root',
'PASSWORD': 'root',
'HOST': 'localhost',
'PORT': '3306',
}
}
#手动刷新缓存间隔时间（单位秒）
CACHE_FLUSH_TIME = 10
#自动刷新缓存间隔时间（单位秒）
AUTO_CACHE_FLUSH_TIME = 60
```
### uwsgi配置简单说明

关于uwsgi的配置，这里给出一个实例：harpc_admin.ini

```python
[uwsgi]
http = 0.0.0.0:8000
master = true
module = harpc_admin.wsgi:application
pythonpath = ../
chdir = ../
processes = 1 ;请特别注意，本web系统未做进程安全控制，配置多个进程会出现进程安全问题
buffer-size = 65536
pidfile = harpc_admin.pid
enable-threads = 1
```
常见问题如提示端口占用请修改http参数，如提示找不到harpc_admin.wsgi:application模块，请修改pythonpath，chdir参数指向源码根目录的绝对路径。
