#include "repository/InventoryRepository.h"
#include <fstream>
#include <sstream>
#include <iostream>
#include <cctype>
#include <vector>

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
}

void InventoryRepository::loadFromCsv(const std::string&path)
{
    stock.clear();

    std::ifstream ifs(path);
    if(!ifs.is_open())return;

    std::string line;
    if(!std::getline(ifs,line))return; // header

    while(std::getline(ifs,line))
    {
        if(line.empty())continue;
        auto cols=split(line,',');
        if(cols.size()<2)continue;

        try
        {
            int materialId=std::stoi(trim(cols[0]));
            int quantity=std::stoi(trim(cols[1]));
            if(quantity>0)stock[materialId]=quantity;
        }
        catch(...)
        {
            std::cerr<<"InventoryRepository::loadFromCsv parse error line: "<<line<<"\n";
        }
    }
}

void InventoryRepository::saveToCsv(const std::string&path)const
{
    std::ofstream ofs(path,std::ios::trunc);
    if(!ofs.is_open())
    {
        std::cerr<<"InventoryRepository::saveToCsv open failed: "<<path<<"\n";
        return;
    }

    ofs<<"materialId,quantity\n";
    for(const auto&kv:stock)
    {
        ofs<<kv.first<<","<<kv.second<<"\n";
    }
}

int InventoryRepository::getQuantity(int materialId)const
{
    auto it=stock.find(materialId);
    if(it==stock.end())return 0;
    return it->second;
}

void InventoryRepository::setQuantity(int materialId,int quantity)
{
    if(quantity<=0)stock.erase(materialId);
    else stock[materialId]=quantity;
}

const std::unordered_map<int,int>&InventoryRepository::getAll()const
{
    return stock;
}
