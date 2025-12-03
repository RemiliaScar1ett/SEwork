// server/include/protocol/Response.h
#pragma once
#include <string>

struct Response
{
    std::string requestId;
    bool success;
    std::string errorCode;
    std::string message;
    std::string dataJson;   // data 对象的 JSON 字符串
};
