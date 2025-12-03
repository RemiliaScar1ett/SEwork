#include "service/OrderService.h"
#include <stdexcept>

OrderService::OrderService(OrderRepository&oRepo,
                           SupplierRepository&sRepo,
                           MaterialRepository&mRepo)
    :orderRepo(oRepo),supplierRepo(sRepo),materialRepo(mRepo)
{
}

int OrderService::createOrder(int supplierId,const std::string&date,const User&currentUser)
{
    // 检查供应商是否存在
    auto optSup=supplierRepo.findById(supplierId);
    if(!optSup.has_value())
        throw std::runtime_error("supplier not found");

    PurchaseOrder o;
    o.supplierId=supplierId;
    o.date=date;
    o.status=OrderStatus::New;
    o.createdBy=currentUser.id;

    int id=orderRepo.addOrder(o);
    return id;
}

int OrderService::addOrderItem(int orderId,int materialId,int quantity,double price,
                               const User&currentUser)
{
    if(quantity<=0)
        throw std::runtime_error("quantity must be positive");

    auto optOrder=orderRepo.findOrderById(orderId);
    if(!optOrder.has_value())
        throw std::runtime_error("order not found");

    auto optMat=materialRepo.findById(materialId);
    if(!optMat.has_value())
        throw std::runtime_error("material not found");

    PurchaseOrderItem item;
    item.orderId=orderId;
    item.materialId=materialId;
    item.quantity=quantity;
    item.price=price;
    item.receivedQuantity=0;

    int itemId=orderRepo.addOrderItem(item);
    return itemId;
}

std::vector<OrderSummary> OrderService::listOrders() const
{
    auto orders=orderRepo.getAllOrders();
    std::vector<OrderSummary> result;
    result.reserve(orders.size());

    for(const auto&o:orders)
    {
        OrderSummary s;
        s.order=o;
        auto optSup=supplierRepo.findById(o.supplierId);
        s.supplierName=optSup.has_value()?optSup->name:"<Unknown>";
        result.push_back(s);
    }
    return result;
}

std::optional<OrderDetail> OrderService::getOrderDetail(int orderId) const
{
    auto optOrder=orderRepo.findOrderById(orderId);
    if(!optOrder.has_value())
        return std::nullopt;

    OrderDetail detail;
    detail.order=optOrder.value();

    auto optSup=supplierRepo.findById(detail.order.supplierId);
    detail.supplierName=optSup.has_value()?optSup->name:"<Unknown>";

    detail.items=orderRepo.findItemsByOrderId(orderId);
    return detail;
}

std::vector<OrderSummary> OrderService::listOrdersBySupplier(int supplierId) const
{
    auto orders=orderRepo.getAllOrders();
    std::vector<OrderSummary> result;

    for(const auto&o:orders)
    {
        if(o.supplierId!=supplierId)
            continue;

        OrderSummary s;
        s.order=o;
        auto optSup=supplierRepo.findById(o.supplierId);
        s.supplierName=optSup.has_value()?optSup->name:"<Unknown>";
        result.push_back(s);
    }
    return result;
}

std::vector<OrderSummary> OrderService::listOpenOrders() const
{
    auto orders=orderRepo.getAllOrders();
    std::vector<OrderSummary> result;

    for(const auto&o:orders)
    {
        if(o.status==OrderStatus::Closed)
            continue;

        OrderSummary s;
        s.order=o;
        auto optSup=supplierRepo.findById(o.supplierId);
        s.supplierName=optSup.has_value()?optSup->name:"<Unknown>";
        result.push_back(s);
    }
    return result;
}

void OrderService::updateOrderStatus(int orderId)
{
    auto optOrder=orderRepo.findOrderById(orderId);
    if(!optOrder.has_value())
        return;

    PurchaseOrder order=optOrder.value();
    auto items=orderRepo.findItemsByOrderId(orderId);

    if(items.empty())
    {
        order.status=OrderStatus::New;
        orderRepo.updateOrder(order);
        return;
    }

    bool anyReceived=false;
    bool allFull=true;

    for(const auto&it:items)
    {
        if(it.receivedQuantity>0)
            anyReceived=true;
        if(it.receivedQuantity<it.quantity)
            allFull=false;
    }

    if(!anyReceived)
        order.status=OrderStatus::New;
    else if(allFull)
        order.status=OrderStatus::Closed;
    else
        order.status=OrderStatus::Partial;

    orderRepo.updateOrder(order);
}
