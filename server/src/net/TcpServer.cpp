#include "net/TcpServer.h"
#include <iostream>

TcpServer::TcpServer(int port,RequestDispatcher&dispatcher)
    :port(port),dispatcher(dispatcher),listenSock(INVALID_SOCKET)
{
}

TcpServer::~TcpServer()
{
    if(listenSock!=INVALID_SOCKET) closeSocket(listenSock);
    WSACleanup();
}

bool TcpServer::initWinsock()
{
    WSADATA wsa;
    int r=WSAStartup(MAKEWORD(2,2),&wsa);
    if(r!=0)
    {
        std::cerr<<"WSAStartup failed, code "<<r<<"\n";
        return false;
    }
    return true;
}

bool TcpServer::initSocket()
{
    if(!initWinsock()) return false;

    listenSock=::socket(AF_INET,SOCK_STREAM,IPPROTO_TCP);
    if(listenSock==INVALID_SOCKET)
    {
        std::cerr<<"socket() failed, code "<<WSAGetLastError()<<"\n";
        return false;
    }

    BOOL opt=TRUE;
    setsockopt(listenSock,SOL_SOCKET,SO_REUSEADDR,(const char*)&opt,sizeof(opt));

    sockaddr_in addr;
    memset(&addr,0,sizeof(addr));
    addr.sin_family=AF_INET;
    addr.sin_addr.s_addr=inet_addr("127.0.0.1");
    addr.sin_port=htons((u_short)port);

    if(bind(listenSock,(sockaddr*)&addr,sizeof(addr))==SOCKET_ERROR)
    {
        std::cerr<<"bind() failed, code "<<WSAGetLastError()<<"\n";
        return false;
    }

    if(listen(listenSock,5)==SOCKET_ERROR)
    {
        std::cerr<<"listen() failed, code "<<WSAGetLastError()<<"\n";
        return false;
    }

    std::cout<<"Server listening on 127.0.0.1:"<<port<<"\n";
    return true;
}

void TcpServer::start()
{
    if(!initSocket()) return;

    while(true)
    {
        sockaddr_in clientAddr;
        int len=sizeof(clientAddr);
        SOCKET clientSock=accept(listenSock,(sockaddr*)&clientAddr,&len);
        if(clientSock==INVALID_SOCKET)
        {
            std::cerr<<"accept() failed, code "<<WSAGetLastError()<<"\n";
            continue;
        }

        handleClient(clientSock);
        closeSocket(clientSock);
    }
}

void TcpServer::closeSocket(SOCKET s)
{
    closesocket(s);
}

bool TcpServer::readLine(SOCKET clientSock,std::string&out)
{
    out.clear();
    char c;
    while(true)
    {
        int n=recv(clientSock,&c,1,0);
        if(n<=0) return false;
        if(c=='\n') break;
        out.push_back(c);
    }
    return true;
}

bool TcpServer::sendAll(SOCKET clientSock,const std::string&data)
{
    const char*buf=data.c_str();
    int total=(int)data.size();
    int sent=0;
    while(sent<total)
    {
        int n=send(clientSock,buf+sent,total-sent,0);
        if(n<=0) return false;
        sent+=n;
    }
    return true;
}

void TcpServer::handleClient(SOCKET clientSock)
{
    std::string line;
    if(!readLine(clientSock,line))
    {
        std::cerr<<"readLine failed\n";
        return;
    }
    std::cout<<"[Server] recv: "<<line<<"\n";

    std::string resp=dispatcher.handleRequest(line);
    if(!resp.empty() && resp.back()!='\n') resp.push_back('\n');

    if(!sendAll(clientSock,resp))
    {
        std::cerr<<"sendAll failed\n";
        return;
    }
    std::cout<<"[Server] sent: "<<resp;
}
