// server/include/protocol/Request.h
#pragma once
#include <string>

struct Request
{
    std::string requestId;
    std::string action;
    std::string token;   // 从 JSON 的 auth.token 取
    std::string rawData; // data 对象的 JSON 字符串，后面用 JSON 库解析
};
