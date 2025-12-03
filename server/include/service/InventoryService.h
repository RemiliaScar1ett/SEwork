// server/include/service/InventoryService.h
#pragma once
#include <unordered_map>
#include "repository/InventoryRepository.h"
#include "repository/OrderRepository.h"
#include "domain/User.h"

struct StockInfo
{
    int materialId;
    int quantity;
};

class InventoryService
{
public:
    InventoryService(InventoryRepository&invRepo,OrderRepository&orderRepo);

    // 收货：更新订单行 + 库存 + 订单状态
    bool receiveGoods(int orderId,int itemId,int quantity,const User&currentUser,
                      bool&orderClosedOut,int&newItemReceivedQty,int&newStockQty);

    int getStock(int materialId) const;
    std::vector<StockInfo> listStocks() const;

private:
    InventoryRepository&inventoryRepo;
    OrderRepository&orderRepo;
};
