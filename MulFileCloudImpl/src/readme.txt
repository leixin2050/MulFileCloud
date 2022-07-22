框架留给外部的使用接口分别为：
1、OriginalSource 资源

2、RequestSourceBase 资源请求者
注册中心 Client - RMIServer
接收文件 Server - A
发送文件片段信息 Client - B

3、HolderSourcePool 资源拥有者
注册中心 Client - NIOServer
接收文件片段信息 Server - B
发送文件 Client - A
