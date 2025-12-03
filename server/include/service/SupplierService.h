// server/include/service/SupplierService.h
#pragma once
#include <vector>
#include "domain/Supplier.h"
#include "repository/SupplierRepository.h"
#include "domain/User.h"

class SupplierService
{
public:
    SupplierService(SupplierRepository&repo);

    int createSupplier(const std::string&name,const std::string&contact,
                       const std::string&phone,const User&currentUser);
    std::vector<Supplier> listSuppliers() const;

private:
    SupplierRepository&supplierRepo;

    void ensureAdmin(const User&currentUser) const;
};
