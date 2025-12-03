// server/include/repository/SupplierRepository.h
#pragma once
#include <vector>
#include <string>
#include <optional>
#include "domain/Supplier.h"

class SupplierRepository
{
public:
    void loadFromCsv(const std::string&path);
    void saveToCsv(const std::string&path) const;

    std::optional<Supplier> findById(int id) const;
    int addSupplier(const Supplier&s);
    bool updateSupplier(const Supplier&s);
    const std::vector<Supplier>& getAll() const;

    int nextId() const;

private:
    std::vector<Supplier> suppliers;
};
