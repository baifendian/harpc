**目录 (Table of Contents)**

[TOCM] 

### 管理系统

- 管理系统主要展示两个页面，service总览、server管理&client列表；
- 服务总览页面罗列出所有的服务信息，包括：服务名、server列表、client列表、操作；
- server管理页面：展示每个server的配置信息和支持查看统计图表；
- client管理页面：展示每个client的配置信息和支持查看统计图表;
- 服务管理需要支持权限管理，包括两类用户admin和guest：admin登陆需要密码，运行操作服务节点（添加和删除）；guest不需要用户名密码，并且只有查看权限；
- 只有admin用户手动添加的server信息才允许删除，用户自行注册的server信息不允许删除；
- 管理系统内置缓存，会缓存整个ZK以减少查询ZK，减轻ZK负担，缓存的更新使用2个种方式，ZK监听器自动更新 or 用户手动/半自动触发式，考虑到ZK负担，默认关闭了ZK自动监听更新；

### 管理系统依赖

管理系统依赖环境：
python 2.7 ；django 1.8.2+ ；kazoo ；uwsgi ；mysql ；

### 部署方式

1. 下载源码包

2. 进入mysql数据库执行管理系统数据库创建命令
	`CREATE DATABASE harpc_admin;`

3. 根据mysql配置信息，ZK信息配置源码包配置文件，具体配置方式参见配置详解

4. 进入源码包根目录执行命令，该命令用于初始化表结构，使用该命令是可根据提示创建admin账号
	`python manage.py syncdb`

5. 在源码包根目录执行命令，进入系统DEBUG模式，如果启动成功则配置一切正常
	`python manage.py runserver 0.0.0.0:8000`

6. 在系统DEBUG模式下登录http://localhost:8000/admin/ 该地址为系统后台管理位置，由django原生提供，这设置其他用户等信息

7. 关闭DEBUG模式，创建uwsgi配置文件，修改源码包配置文件(参见配置详解)，使用uwsgi启动管理系统部署完毕。

### 配置文件详解

- 配置文件位于源码包根目录下/manage/settings.py文件
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
#管理系统在zk内注册的用户名
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
#手动刷新缓存间隔时间
CACHE_FLUSH_TIME = 10
#自动刷新缓存间隔时间
AUTO_CACHE_FLUSH_TIME = 60
```