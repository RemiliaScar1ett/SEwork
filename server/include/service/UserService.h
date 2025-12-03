// server/include/service/UserService.h
#pragma once
#include <vector>
#include "domain/User.h"
#include "repository/UserRepository.h"

class UserService
{
public:
    UserService(UserRepository&userRepo);

    int createUser(const User&newUser,const User&currentUser);
    std::vector<User> listUsers(const User&currentUser) const;
    bool resetPassword(int userId,const std::string&newPassword,const User&currentUser);

private:
    UserRepository&userRepo;

    void ensureAdmin(const User&currentUser) const;
};
