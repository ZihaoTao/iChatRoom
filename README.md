# iChatRoom

## Introduction:

Java NIO Socket data transfer model:
![Data transfer model](https://github.com/ZihaoTao/iChatRoom/blob/master/Diagram.jpg)

1. Client uses UDP broadcast to find Server in LAN, and the Server uses UDP to send its 
IP and port back to client.
2. Client uses the IP and port to build TCP connection with Server.
3. NIO optimization: replace threads with thread pools to receive and send data, decrease
number of threads and CPU usage of Server. 

<figure class="half">
<img src="https://github.com/ZihaoTao/iChatRoom/blob/master/previous.jpg"> 
<img src="https://github.com/ZihaoTao/iChatRoom/blob/master/now.jpg">
</figure>

