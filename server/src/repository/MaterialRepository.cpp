#include "repository/MaterialRepository.h"
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

void MaterialRepository::loadFromCsv(const std::string&path)
{
    materials.clear();

    std::ifstream ifs(path);
    if(!ifs.is_open())return;

    std::string line;
    if(!std::getline(ifs,line))return; // header

    while(std::getline(ifs,line))
    {
        if(line.empty())continue;
        auto cols=split(line,',');
        if(cols.size()<4)continue;

        try
        {
            Material m;
            m.id=std::stoi(trim(cols[0]));
            m.name=trim(cols[1]);
            m.spec=trim(cols[2]);
            m.unit=trim(cols[3]);
            materials.push_back(m);
        }
        catch(...)
        {
            std::cerr<<"MaterialRepository::loadFromCsv parse error line: "<<line<<"\n";
        }
    }
}

void MaterialRepository::saveToCsv(const std::string&path)const
{
    std::ofstream ofs(path,std::ios::trunc);
    if(!ofs.is_open())
    {
        std::cerr<<"MaterialRepository::saveToCsv open failed: "<<path<<"\n";
        return;
    }

    ofs<<"id,name,spec,unit\n";
    for(const auto&m:materials)
    {
        ofs<<m.id<<","<<m.name<<","<<m.spec<<","<<m.unit<<"\n";
    }
}

std::optional<Material>MaterialRepository::findById(int id)const
{
    for(const auto&m:materials)
        if(m.id==id)return m;
    return std::nullopt;
}

int MaterialRepository::addMaterial(const Material&m)
{
    int id=nextId();
    Material copy=m;
    copy.id=id;
    materials.push_back(copy);
    return id;
}

bool MaterialRepository::updateMaterial(const Material&m)
{
    for(auto&x:materials)
    {
        if(x.id==m.id)
        {
            x=m;
            return true;
        }
    }
    return false;
}

const std::vector<Material>&MaterialRepository::getAll()const
{
    return materials;
}

int MaterialRepository::nextId()const
{
    int maxId=0;
    for(const auto&m:materials)
        if(m.id>maxId)maxId=m.id;
    return maxId+1;
}
