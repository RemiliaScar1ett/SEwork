// server/include/persistence/DataStorage.h
#pragma once
#include <string>
#include "repository/UserRepository.h"
#include "repository/MaterialRepository.h"
#include "repository/SupplierRepository.h"
#include "repository/OrderRepository.h"
#include "repository/InventoryRepository.h"

class DataStorage
{
public:
    // baseDir: 数据文件所在目录，如 "data"
    DataStorage(const std::string&baseDir);

    // 在程序启动时调用：从 CSV 加载所有数据
    void loadAll();

    // 在程序退出时调用：把内存数据写回 CSV
    void saveAll()const;

    // 对外公开的仓储对象
    UserRepository userRepo;
    MaterialRepository materialRepo;
    SupplierRepository supplierRepo;
    OrderRepository orderRepo;
    InventoryRepository inventoryRepo;

private:
    std::string baseDir;

    std::string pathUsers()const;
    std::string pathMaterials()const;
    std::string pathSuppliers()const;
    std::string pathOrders()const;
    std::string pathOrderItems()const;
    std::string pathInventory()const;
};
