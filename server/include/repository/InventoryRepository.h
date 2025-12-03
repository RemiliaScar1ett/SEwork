// server/include/repository/InventoryRepository.h
#pragma once
#include <unordered_map>
#include <string>
#include "domain/InventoryRecord.h"

class InventoryRepository
{
public:
    void loadFromCsv(const std::string&path);
    void saveToCsv(const std::string&path) const;

    int getQuantity(int materialId) const;
    void setQuantity(int materialId,int quantity);
    const std::unordered_map<int,int>& getAll() const;

private:
    std::unordered_map<int,int> stock; // materialId->quantity
};
