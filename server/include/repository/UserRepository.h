// server/include/repository/UserRepository.h
#pragma once
#include <vector>
#include <string>
#include <optional>
#include "domain/User.h"

class UserRepository
{
public:
    void loadFromCsv(const std::string&path);
    void saveToCsv(const std::string&path) const;

    std::optional<User> findById(int id) const;
    std::optional<User> findByUsername(const std::string&username) const;

    int addUser(const User&user);          // 返回新id
    bool updateUser(const User&user);      // 按id更新
    const std::vector<User>& getAll() const;

    int nextId() const;                    // 基于当前集合计算

private:
    std::vector<User> users;
};
