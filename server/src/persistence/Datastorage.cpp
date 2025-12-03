// server/src/persistence/DataStorage.cpp
#include "persistence/DataStorage.h"
#include <filesystem>
#include <iostream>

DataStorage::DataStorage(const std::string&dir)
    :baseDir(dir)
{
}

std::string DataStorage::pathUsers()const
{
    return baseDir+"/users.csv";
}

std::string DataStorage::pathMaterials()const
{
    return baseDir+"/materials.csv";
}

std::string DataStorage::pathSuppliers()const
{
    return baseDir+"/suppliers.csv";
}

std::string DataStorage::pathOrders()const
{
    return baseDir+"/orders.csv";
}

std::string DataStorage::pathOrderItems()const
{
    return baseDir+"/order_items.csv";
}

std::string DataStorage::pathInventory()const
{
    return baseDir+"/inventory.csv";
}

void DataStorage::loadAll()
{
    // 确保目录存在（C++17 filesystem）
    try
    {
        std::filesystem::create_directories(baseDir);
    }
    catch(...)
    {
        std::cerr<<"[DataStorage] create_directories failed for "<<baseDir<<"\n";
    }

    userRepo.loadFromCsv(pathUsers());
    materialRepo.loadFromCsv(pathMaterials());
    supplierRepo.loadFromCsv(pathSuppliers());
    orderRepo.loadFromCsv(pathOrders(),pathOrderItems());
    inventoryRepo.loadFromCsv(pathInventory());
}

void DataStorage::saveAll()const
{
    try
    {
        std::filesystem::create_directories(baseDir);
    }
    catch(...)
    {
        std::cerr<<"[DataStorage] create_directories failed for "<<baseDir<<"\n";
    }

    userRepo.saveToCsv(pathUsers());
    materialRepo.saveToCsv(pathMaterials());
    supplierRepo.saveToCsv(pathSuppliers());
    orderRepo.saveToCsv(pathOrders(),pathOrderItems());
    inventoryRepo.saveToCsv(pathInventory());
}
