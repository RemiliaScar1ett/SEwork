// server/include/domain/PurchaseOrder.h
#pragma once
#include <string>

enum class OrderStatus
{
    New,
    Partial,
    Closed
};

struct PurchaseOrder
{
    int id;
    int supplierId;
    std::string date;
    OrderStatus status;
    int createdBy;

    PurchaseOrder():id(0),supplierId(0),status(OrderStatus::New),createdBy(0){}
};

struct PurchaseOrderItem
{
    int id;
    int orderId;
    int materialId;
    int quantity;
    double price;
    int receivedQuantity;

    PurchaseOrderItem()
        :id(0),orderId(0),materialId(0),quantity(0),price(0.0),receivedQuantity(0){}
};
