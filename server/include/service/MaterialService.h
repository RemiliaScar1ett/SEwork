// server/include/service/MaterialService.h
#pragma once
#include <vector>
#include "domain/Material.h"
#include "repository/MaterialRepository.h"
#include "domain/User.h"

class MaterialService
{
public:
    MaterialService(MaterialRepository&repo);

    int createMaterial(const std::string&name,const std::string&spec,
                       const std::string&unit,const User&currentUser);
    std::vector<Material> listMaterials() const;

private:
    MaterialRepository&materialRepo;

    void ensureAdmin(const User&currentUser) const;
};
