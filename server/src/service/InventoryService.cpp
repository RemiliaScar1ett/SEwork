#include "service/InventoryService.h"

InventoryService::InventoryService(InventoryRepository&invRepo,OrderRepository&oRepo)
    :inventoryRepo(invRepo),orderRepo(oRepo)
{
}

bool InventoryService::receiveGoods(int orderId,int itemId,int quantity,
                                    const User&currentUser,
                                    bool&orderClosedOut,int&newItemReceivedQty,int&newStockQty)
{
    orderClosedOut=false;
    newItemReceivedQty=0;
    newStockQty=0;

    if(quantity<=0)
        return false;

    auto optOrder=orderRepo.findOrderById(orderId);
    if(!optOrder.has_value())
        return false;

    auto optItem=orderRepo.findItemById(itemId);
    if(!optItem.has_value())
        return false;

    PurchaseOrder order=optOrder.value();
    PurchaseOrderItem item=optItem.value();

    if(item.orderId!=orderId)
        return false;

    if(item.receivedQuantity+quantity>item.quantity)
        return false;

    // 更新明细收货数量
    item.receivedQuantity+=quantity;
    newItemReceivedQty=item.receivedQuantity;
    orderRepo.updateOrderItem(item);

    // 更新库存
    int oldQty=inventoryRepo.getQuantity(item.materialId);
    int stock=oldQty+quantity;
    inventoryRepo.setQuantity(item.materialId,stock);
    newStockQty=stock;

    // 更新订单状态
    auto items=orderRepo.findItemsByOrderId(orderId);
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
    orderClosedOut=(order.status==OrderStatus::Closed);
    return true;
}

int InventoryService::getStock(int materialId) const
{
    return inventoryRepo.getQuantity(materialId);
}

std::vector<StockInfo> InventoryService::listStocks() const
{
    const auto&map=inventoryRepo.getAll();
    std::vector<StockInfo> result;
    result.reserve(map.size());
    for(auto&kv:map)
    {
        StockInfo s;
        s.materialId=kv.first;
        s.quantity=kv.second;
        result.push_back(s);
    }
    return result;
}
