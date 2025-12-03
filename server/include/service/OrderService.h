// server/include/service/OrderService.h
#pragma once
#include <vector>
#include "domain/PurchaseOrder.h"
#include "domain/User.h"
#include "repository/OrderRepository.h"
#include "repository/SupplierRepository.h"
#include "repository/MaterialRepository.h"

struct OrderSummary
{
    PurchaseOrder order;
    std::string supplierName;
};

struct OrderDetail
{
    PurchaseOrder order;
    std::string supplierName;
    std::vector<PurchaseOrderItem> items;
};

class OrderService
{
public:
    OrderService(OrderRepository&orderRepo,
                 SupplierRepository&supplierRepo,
                 MaterialRepository&materialRepo);

    int createOrder(int supplierId,const std::string&date,const User&currentUser);
    int addOrderItem(int orderId,int materialId,int quantity,double price,const User&currentUser);

    std::vector<OrderSummary> listOrders() const;
    std::optional<OrderDetail> getOrderDetail(int orderId) const;
    std::vector<OrderSummary> listOrdersBySupplier(int supplierId) const;
    std::vector<OrderSummary> listOpenOrders() const;

    void updateOrderStatus(int orderId);

private:
    OrderRepository&orderRepo;
    SupplierRepository&supplierRepo;
    MaterialRepository&materialRepo;
};
