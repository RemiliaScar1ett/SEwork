#include "repository/SupplierRepository.h"
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
}

void SupplierRepository::loadFromCsv(const std::string&path)
{
    suppliers.clear();

    std::ifstream ifs(path);
    if(!ifs.is_open())return;

    std::string line;
    if(!std::getline(ifs,line))return;

    while(std::getline(ifs,line))
    {
        if(line.empty())continue;
        auto cols=split(line,',');
        if(cols.size()<4)continue;

        try
        {
            Supplier s;
            s.id=std::stoi(trim(cols[0]));
            s.name=trim(cols[1]);
            s.contact=trim(cols[2]);
            s.phone=trim(cols[3]);
            suppliers.push_back(s);
        }
        catch(...)
        {
            std::cerr<<"SupplierRepository::loadFromCsv parse error line: "<<line<<"\n";
        }
    }
}

void SupplierRepository::saveToCsv(const std::string&path)const
{
    std::ofstream ofs(path,std::ios::trunc);
    if(!ofs.is_open())
    {
        std::cerr<<"SupplierRepository::saveToCsv open failed: "<<path<<"\n";
        return;
    }

    ofs<<"id,name,contact,phone\n";
    for(const auto&s:suppliers)
    {
        ofs<<s.id<<","<<s.name<<","<<s.contact<<","<<s.phone<<"\n";
    }
}

std::optional<Supplier>SupplierRepository::findById(int id)const
{
    for(const auto&s:suppliers)
        if(s.id==id)return s;
    return std::nullopt;
}

int SupplierRepository::addSupplier(const Supplier&s)
{
    int id=nextId();
    Supplier copy=s;
    copy.id=id;
    suppliers.push_back(copy);
    return id;
}

bool SupplierRepository::updateSupplier(const Supplier&s)
{
    for(auto&x:suppliers)
    {
        if(x.id==s.id)
        {
            x=s;
            return true;
        }
    }
    return false;
}

const std::vector<Supplier>&SupplierRepository::getAll()const
{
    return suppliers;
}

int SupplierRepository::nextId()const
{
    int maxId=0;
    for(const auto&s:suppliers)
        if(s.id>maxId)maxId=s.id;
    return maxId+1;
}
