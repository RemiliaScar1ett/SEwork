#include "repository/UserRepository.h"
#include <fstream>
#include <sstream>
#include <iostream>
#include <cctype>

namespace
{
std::string trim(const std::string&s)
{
    size_t start=0;
    while(start<s.size()&&std::isspace((unsigned char)s[start]))start++;
    size_t end=s.size();
    while(end>start&&std::isspace((unsigned char)s[end-1]))end--;
    return s.substr(start,end-start);
}

std::vector<std::string>split(const std::string&s,char delim)
{
    std::vector<std::string>res;
    std::string cur;
    std::istringstream iss(s);
    while(std::getline(iss,cur,delim))
        res.push_back(cur);
    return res;
}

UserRole parseRole(const std::string&s)
{
    std::string t=trim(s);
    for(char&c:t)c=(char)std::tolower((unsigned char)c);
    if(t=="admin")return UserRole::Admin;
    return UserRole::User;
}

std::string roleToString(UserRole role)
{
    return role==UserRole::Admin?"admin":"user";
}
}

void UserRepository::loadFromCsv(const std::string&path)
{
    users.clear();

    std::ifstream ifs(path);
    if(!ifs.is_open())
    {
        // 文件不存在时保持空集合即可
        return;
    }

    std::string line;
    // 读表头
    if(!std::getline(ifs,line))return;

    while(std::getline(ifs,line))
    {
        if(line.empty())continue;
        auto cols=split(line,',');
        if(cols.size()<4)continue;

        try
        {
            User u;
            u.id=std::stoi(trim(cols[0]));
            u.username=trim(cols[1]);
            u.password=trim(cols[2]);
            u.role=parseRole(cols[3]);
            users.push_back(u);
        }
        catch(...)
        {
            std::cerr<<"UserRepository::loadFromCsv parse error line: "<<line<<"\n";
        }
    }
}

void UserRepository::saveToCsv(const std::string&path)const
{
    std::ofstream ofs(path,std::ios::trunc);
    if(!ofs.is_open())
    {
        std::cerr<<"UserRepository::saveToCsv open failed: "<<path<<"\n";
        return;
    }

    ofs<<"id,username,password,role\n";
    for(const auto&u:users)
    {
        ofs<<u.id<<","<<u.username<<","<<u.password<<","<<roleToString(u.role)<<"\n";
    }
}

std::optional<User>UserRepository::findById(int id)const
{
    for(const auto&u:users)
        if(u.id==id)return u;
    return std::nullopt;
}

std::optional<User>UserRepository::findByUsername(const std::string&username)const
{
    for(const auto&u:users)
        if(u.username==username)return u;
    return std::nullopt;
}

int UserRepository::addUser(const User&user)
{
    int id=nextId();
    User u=user;
    u.id=id;
    users.push_back(u);
    return id;
}

bool UserRepository::updateUser(const User&user)
{
    for(auto&u:users)
    {
        if(u.id==user.id)
        {
            u=user;
            return true;
        }
    }
    return false;
}

const std::vector<User>&UserRepository::getAll()const
{
    return users;
}

int UserRepository::nextId()const
{
    int maxId=0;
    for(const auto&u:users)
        if(u.id>maxId)maxId=u.id;
    return maxId+1;
}
