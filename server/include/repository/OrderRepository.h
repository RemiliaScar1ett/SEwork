// server/include/repository/OrderRepository.h
#pragma once
#include <vector>
#include <string>
#include <optional>
#include "domain/PurchaseOrder.h"

class OrderRepository
{
public:
    void loadFromCsv(const std::string&orderPath,const std::string&itemPath);
    void saveToCsv(const std::string&orderPath,const std::string&itemPath) const;

    std::optional<PurchaseOrder> findOrderById(int id) const;
    std::vector<PurchaseOrder> getAllOrders() const;

    int addOrder(const PurchaseOrder&o);
    bool updateOrder(const PurchaseOrder&o);

    std::vector<PurchaseOrderItem> findItemsByOrderId(int orderId) const;
    std::optional<PurchaseOrderItem> findItemById(int itemId) const;
    int addOrderItem(const PurchaseOrderItem&item);
    bool updateOrderItem(const PurchaseOrderItem&item);

    int nextOrderId() const;
    int nextOrderItemId() const;

private:
    std::vector<PurchaseOrder> orders;
    std::vector<PurchaseOrderItem> items;
};
