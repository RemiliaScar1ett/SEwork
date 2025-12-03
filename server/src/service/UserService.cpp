#include "service/UserService.h"
#include <stdexcept>

UserService::UserService(UserRepository&repo)
    :userRepo(repo)
{
}

void UserService::ensureAdmin(const User&currentUser) const
{
    if(currentUser.role!=UserRole::Admin)
        throw std::runtime_error("permission denied: not admin");
}

int UserService::createUser(const User&newUser,const User&currentUser)
{
    ensureAdmin(currentUser);

    // 检查用户名是否重复
    auto existing=userRepo.findByUsername(newUser.username);
    if(existing.has_value())
        throw std::runtime_error("username already exists");

    // 直接交给仓储分配id并插入
    int id=userRepo.addUser(newUser);
    return id;
}

std::vector<User> UserService::listUsers(const User&currentUser) const
{
    ensureAdmin(currentUser);

    const auto&all=userRepo.getAll();
    std::vector<User> result;
    result.reserve(all.size());
    for(const auto&u:all)
        result.push_back(u);
    return result;
}

bool UserService::resetPassword(int userId,const std::string&newPassword,const User&currentUser)
{
    ensureAdmin(currentUser);

    auto optUser=userRepo.findById(userId);
    if(!optUser.has_value())
        return false;

    User u=optUser.value();
    u.password=newPassword;
    return userRepo.updateUser(u);
}
