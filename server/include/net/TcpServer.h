#pragma once

#include <string>
#include <winsock2.h>
#include <functional>
#include "protocol/RequestDispatcher.h"

// 让编译器自动链接 ws2_32.lib（CMake 里也 link 了的话不冲突）
#pragma comment(lib,"ws2_32.lib")

class TcpServer
{
public:
    TcpServer(int port,RequestDispatcher&dispatcher);
    ~TcpServer();

    void start();   // 阻塞循环：接受连接、收一行、回一行
    void setTickCallback(const std::function<void()>&cb);

private:
    int port;
    RequestDispatcher&dispatcher;
    SOCKET listenSock;
    std::function<void()> tickCallback;

    bool initWinsock();
    bool initSocket();
    void closeSocket(SOCKET s);
    void handleClient(SOCKET clientSock);
    bool readLine(SOCKET clientSock,std::string&out);
    bool sendAll(SOCKET clientSock,const std::string&data);
};
