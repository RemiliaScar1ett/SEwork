// server/include/protocol/RequestDispatcher.h
#pragma once
#include <string>
#include "protocol/Request.h"
#include "protocol/Response.h"
#include "service/AuthService.h"
#include "service/UserService.h"
#include "service/MaterialService.h"
#include "service/SupplierService.h"
#include "service/OrderService.h"
#include "service/InventoryService.h"

class RequestDispatcher
{
public:
    RequestDispatcher(AuthService&authService,
                      UserService&userService,
                      MaterialService&materialService,
                      SupplierService&supplierService,
                      OrderService&orderService,
                      InventoryService&inventoryService);

    // 供 TcpServer 调用的唯一入口
    std::string handleRequest(const std::string&requestJson);

private:
    AuthService&authService;
    UserService&userService;
    MaterialService&materialService;
    SupplierService&supplierService;
    OrderService&orderService;
    InventoryService&inventoryService;

    Request parseRequest(const std::string&jsonStr);
    std::string encodeResponse(const Response&resp);

    Response handleLogin(const Request&req);
    Response handleCreateUser(const Request&req,const User&currentUser);
    Response handleListUsers(const Request&req,const User&currentUser);
    Response handleResetPassword(const Request&req,const User&currentUser);

    Response handleCreateMaterial(const Request&req,const User&currentUser);
    Response handleListMaterials(const Request&req);

    Response handleCreateSupplier(const Request&req,const User&currentUser);
    Response handleListSuppliers(const Request&req);

    Response handleCreateOrder(const Request&req,const User&currentUser);
    Response handleAddOrderItem(const Request&req,const User&currentUser);
    Response handleListOrders(const Request&req);
    Response handleGetOrderDetail(const Request&req);
    Response handleListOrdersBySupplier(const Request&req);
    Response handleListOpenOrders(const Request&req);

    Response handleReceiveGoods(const Request&req,const User&currentUser);
    Response handleGetStock(const Request&req);
    Response handleListStocks(const Request&req);

    // 通用工具：鉴权+拿当前用户
    std::optional<User> requireLogin(const Request&req,Response&resp);
};
