// server/include/domain/User.h
#pragma once
#include <string>

enum class UserRole
{
    Admin,
    User
};

struct User
{
    int id;
    std::string username;
    std::string password;   // 简化：明文或简单hash
    UserRole role;

    User():id(0),role(UserRole::User){}
};
