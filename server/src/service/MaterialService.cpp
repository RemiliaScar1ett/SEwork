#include "service/MaterialService.h"
#include <stdexcept>

MaterialService::MaterialService(MaterialRepository&repo) :materialRepo(repo){}

void MaterialService::ensureAdmin(const User&currentUser) const
{
    if(currentUser.role!=UserRole::Admin)
        throw std::runtime_error("permission denied: not admin");
}

int MaterialService::createMaterial(const std::string&name,const std::string&spec,
                                    const std::string&unit,const User&currentUser)
{
    ensureAdmin(currentUser);

    Material m;
    m.name=name;
    m.spec=spec;
    m.unit=unit;

    int id=materialRepo.addMaterial(m);
    return id;
}

std::vector<Material> MaterialService::listMaterials() const
{
    const auto&all=materialRepo.getAll();
    std::vector<Material> result;
    result.reserve(all.size());
    for(const auto&m:all)
        result.push_back(m);
    return result;
}
