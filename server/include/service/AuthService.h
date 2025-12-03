// server/include/service/AuthService.h
#pragma once
#include <string>
#include <unordered_map>
#include <optional>
#include "domain/User.h"
#include "repository/UserRepository.h"

struct AuthResult
{
    bool success;
    std::string token;
    std::string message;
    User user;
};

class AuthService
{
public:
    AuthService(UserRepository&userRepo);

    AuthResult login(const std::string&username,const std::string&password);
    std::optional<User> getUserByToken(const std::string&token) const;

private:
    UserRepository&userRepo;
    std::unordered_map<std::string,User> sessions; // token->User

    std::string generateToken(const User&user) const;
};
