#include "service/SupplierService.h"
#include <stdexcept>

SupplierService::SupplierService(SupplierRepository&repo)
    :supplierRepo(repo)
{
}

void SupplierService::ensureAdmin(const User&currentUser) const
{
    if(currentUser.role!=UserRole::Admin)
        throw std::runtime_error("permission denied: not admin");
}

int SupplierService::createSupplier(const std::string&name,const std::string&contact,
                                    const std::string&phone,const User&currentUser)
{
    ensureAdmin(currentUser);

    Supplier s;
    s.name=name;
    s.contact=contact;
    s.phone=phone;

    int id=supplierRepo.addSupplier(s);
    return id;
}

std::vector<Supplier> SupplierService::listSuppliers() const
{
    const auto&all=supplierRepo.getAll();
    std::vector<Supplier> result;
    result.reserve(all.size());
    for(const auto&s:all)
        result.push_back(s);
    return result;
}
