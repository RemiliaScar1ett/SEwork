// server/include/repository/MaterialRepository.h
#pragma once
#include <vector>
#include <string>
#include <optional>
#include "domain/Material.h"

class MaterialRepository
{
public:
    void loadFromCsv(const std::string&path);
    void saveToCsv(const std::string&path) const;

    std::optional<Material> findById(int id) const;
    int addMaterial(const Material&m);
    bool updateMaterial(const Material&m);
    const std::vector<Material>& getAll() const;

    int nextId() const;

private:
    std::vector<Material> materials;
};
