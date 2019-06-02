# iChatRoom

## Introduction:

Java NIO Socket data transfer model:
![Data transfer model](https://github.com/ZihaoTao/iChatRoom/blob/master/Diagram.jpg)

1. Client uses UDP broadcast to find Server in LAN, and the Server uses UDP to send its 
IP and port back to client.
2. Client uses the IP and port to build TCP connection with Server.
3. NIO optimization: replace threads with thread pools to receive and send data, decrease
number of threads and CPU usage of Server. 

![previous](https://github.com/ZihaoTao/iChatRoom/blob/master/previous.png)


## Installation

JDK: 
* create file:
```
> cd /
> mkdir developer
> cd developer
```
* download:
JDK url will be invalid after a period of time. Please download on www.oracle.com
```
> wget http://xxx/jdk-8u144-linux-x64xxxxx.rpm
```
* remove suffix
```
> mv jdk-8u144-linux-x64.rpm jdk-8u144-linux-x64.rpmxxxxxxxxx
```
* grant privileges
```
> chmod 777 jdk-8u144-linux-x64.rpm
```
* installation
```
> rpm -ivh jdk-8u144-linux-x64.rpm
```