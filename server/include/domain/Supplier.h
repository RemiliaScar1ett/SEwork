// server/include/domain/Supplier.h
#pragma once
#include <string>

struct Supplier
{
    int id;
    std::string name;
    std::string contact;
    std::string phone;

    Supplier():id(0){}
};
