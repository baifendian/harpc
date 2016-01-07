## 依赖组件安装 ##

### 总体说明 ###
1. 本文提供的安装方式均为**源码安装**，安装包请自行去各官网下载
2. 本文提供的版本均为推荐的版本，可结合实际情况调整，但必须符合依赖包的版本需求
3. 本文的安装过程中的版本号均使用`${version}`代替，实际操作时注意替换

### G++安装（C++编译环境） ###
版本: 5.2.0

安装过程：  
1.使用root账号，将gcc包解压到/opt/software目录下：

```
cd /opt/software
tar -zxvf  gcc-${version}.tar.gz
```
2.在/opt/software/gcc-${version}/contrib/download_prerequisites文件中注释掉wget，避免通过网络下载依赖包：

```
cd  /opt/software/gcc-${version}
vim contrib/download_prerequisites
```
注释掉其中所有的含wget行，若已经注释了则无需注释

3.将software下的gmp-4.3.2.tar.bz2，isl-0.14.tar.bz2，mpc-0.8.1.tar.gz，mpfr-2.4.2.tar.bz2复制且解压到gcc-${version}目录下
```
cd /opt/software
cp gmp-4.3.2.tar.bz2 ./gcc-${version}
cp isl-0.14.tar.bz2 ./gcc-${version}
cp mpc-0.8.1.tar.gz ./gcc-${version}
cp mpfr-2.4.2.tar.bz2 ./gcc-${version}
tar -xf gmp-4.3.2.tar.bz2 -C /opt/software/gcc-${version}
tar -xf isl-0.14.tar.bz2 /opt/software/gcc-${version}
tar -xf mpc-0.8.1.tar.gz -C /opt/software/gcc-${version}
tar -xf mpfr-2.4.2.tar.bz2 -C /opt/software/gcc-${version}
```

4.安装g++
```
./contrib/download_prerequisites
./configure --prefix=/usr/local --enable-checking=release --enable-languages=c,c++  --disable-multilib
make -j23
make install
```

5.确认链接库是否链接正确
检查/usr/lib64/libstdc++.so.6，/usr/local/lib64/libstdc++.so，
/usr/local/lib64/libstdc++.so.6三个链接文件是否存在并链接到
/usr/local/lib64/libstdc++.so.6.0.21
如果没有建立链接，则通过以下方式建立
```
rm -r /usr/lib64/libstdc++.so.6
ln -s /usr/local/lib64/libstdc++.so.6.0.21 /usr/lib64/libstdc++.so.6
rm -r /usr/local/lib64/libstdc++.so 
ln -s  /usr/local/lib64/libstdc++.so.6.0.21 /usr/local/lib64/libstdc++.so 
rm -r /usr/local/lib64/libstdc++.so.6 
ln -s  /usr/local/lib64/libstdc++.so.6.0.21 /usr/local/lib64/libstdc++.so.6 
```
6.修改环境变量:
否则在下面安装cmake时会报找不到libstdc++.so.6.0.21错误
```
vim /etc/profile
```	
添加 export LD_LIBRARY_PATH=/usr/local/lib64:$LD_LIBRARY_PATH
```
source /etc/profile
```

### cmake安装（构建工具） ###
版本: 3.4.0-rc3

安装过程：  
1.使用root账号，将cmake包解压到/opt/software目录下：

```
cd  /opt/software
tar -zxvf  cmake-${version}.tar.gz 
```
	
2.运行bootstrap.sh,安装cmake：
```
cd  /opt/software
tar -zxvf  cmake-${version}.tar.gz 
cd cmake-${version}
./bootstrap 
./configure
make
make install
```
3.运行cmake --help：
若无异常出现，则表示安装成功。

### boost安装（依赖库） ###
版本: 1_55_0

安装过程：

1.使用root账号，将boost包解压到/opt/software目录下：
```
cd  /opt/software
tar -zxvf  boost-${version}.tar.gz 
```
2.运行bootstrap,安装boost：
```
cd /opt/software/boost-${version}
./bootstrap.sh   //运行后得到b2文件
./b2 install
```
若无异常出现，则表示安装成功。

### openssl安装（依赖库） ###
版本：1.0.1e

安装过程：

1.使用root账号，将openssl包解压到/opt/software目录下:
```
cd /opt/software
tar -zxvf  openssl-${version}.tar.gz
./config
make
make install
```
若无异常出现，则表示安装成功。
		
### zlib安装（依赖库） ###
版本：1.2.8

安装过程：

1.使用root账号，将zlib包解压到/opt/software目录下
```
cd /opt/software
tar -zxvf zlib-${version}.tar.gz
cd /opt/software/zlib-${version}
./configure
make
make install
```
若无异常出现，则表示安装成功。

### libevent安装（依赖库） ###
版本：1.4.14b-stable

安装过程：

1.解压libevent-${version}-stable.tar.gz 到/opt/software下并编译安装：
```
cd /opt/software
tar -zxvf  libevent-${version}-stable.tar.gz 
./configure --prefix=/usr/local
make
make install
```	
若无异常出现，则表示安装成功。

### thrift安装 ###
版本: 0.9.2

安装过程：

1.使用root账号，将thrift-${version}.tar.gz包解压到/opt/software目录下：
```
cd  /opt/software
tar -zxvf  thrift-${version}.tar.gz 
cd  thrift-${version}
./configure  --prefix=/usr/local --with-lua=no
make
make install
```
2.查看版本
```
thrift --version
```
3.测试thrift功能：
```
cd /opt/software/thrift-${version}
mkdir aaa
cd aaa
vi hello.thrift
```
在hello.thrift文件中添加
```
service hello{
	string sayHello(),
	i32 add(1:i32 num1, 2:i32 num2)
}
```
保存后，执行下列命令
```
thrift --gen py hello.thrift  ///执行后生产gen-py目录
cd gen-py/hello  //有gen/hellp子目录说明thrfit成功安装
```
