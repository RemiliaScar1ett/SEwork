// server/include/domain/Material.h
#pragma once
#include <string>

struct Material
{
    int id;
    std::string name;
    std::string spec;
    std::string unit;

    Material():id(0){}
};
