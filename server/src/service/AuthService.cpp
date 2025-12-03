#include "service/AuthService.h"
#include <chrono>
#include <sstream>

AuthService::AuthService(UserRepository&repo)
    :userRepo(repo)
{
}

std::string AuthService::generateToken(const User&user) const
{
    static long long counter=0;
    auto now=std::chrono::system_clock::now().time_since_epoch();
    long long ms=std::chrono::duration_cast<std::chrono::milliseconds>(now).count();

    std::ostringstream oss;
    oss<<user.username<<"_"<<user.id<<"_"<<ms<<"_"<<counter++;
    return oss.str();
}

AuthResult AuthService::login(const std::string&username,const std::string&password)
{
    AuthResult result;
    result.success=false;
    result.token="";
    result.message="Invalid username or password";

    auto optUser=userRepo.findByUsername(username);
    if(!optUser.has_value())
        return result;

    User user=optUser.value();
    if(user.password!=password)
        return result;

    std::string token=generateToken(user);
    sessions[token]=user;

    result.success=true;
    result.token=token;
    result.message="OK";
    result.user=user;
    return result;
}

std::optional<User> AuthService::getUserByToken(const std::string&token) const
{
    auto it=sessions.find(token);
    if(it==sessions.end())
        return std::nullopt;
    return it->second;
}
