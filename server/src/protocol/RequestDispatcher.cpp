// server/src/protocol/RequestDispatcher.cpp
#include "protocol/RequestDispatcher.h"
#include "protocol/Request.h"
#include "protocol/Response.h"

#include "thirdparty/json.hpp"   // json.hpp 放在 server/third_party/
#include <iostream>
#include <stdexcept>

using json=nlohmann::json;

namespace
{

std::string roleToString(UserRole role)
{
    return role==UserRole::Admin?"admin":"user";
}

std::string statusToString(OrderStatus st)
{
    switch(st)
    {
    case OrderStatus::Partial:return "PARTIAL";
    case OrderStatus::Closed:return "CLOSED";
    case OrderStatus::New:
    default:return "NEW";
    }
}

Response makeError(const Request&req,const std::string&code,const std::string&msg)
{
    Response r;
    r.requestId=req.requestId;
    r.success=false;
    r.errorCode=code;
    r.message=msg;
    r.dataJson="{}";
    return r;
}

Response makeErrorNoReq(const std::string&code,const std::string&msg)
{
    Response r;
    r.requestId="";
    r.success=false;
    r.errorCode=code;
    r.message=msg;
    r.dataJson="{}";
    return r;
}

Response makeSuccess(const Request&req,const json&data)
{
    Response r;
    r.requestId=req.requestId;
    r.success=true;
    r.errorCode="";
    r.message="OK";
    r.dataJson=data.dump();
    return r;
}

// ===== 各 action 对应的具体处理函数 =====

Response doLogin(AuthService&auth,const Request&req,const json&data)
{
    std::string username=data.at("username").get<std::string>();
    std::string password=data.at("password").get<std::string>();

    AuthResult ar=auth.login(username,password);
    if(!ar.success)
        return makeError(req,"INVALID_CREDENTIALS",ar.message);

    json d;
    d["token"]=ar.token;
    d["userId"]=ar.user.id;
    d["username"]=ar.user.username;
    d["role"]=roleToString(ar.user.role);
    return makeSuccess(req,d);
}

Response doCreateUser(UserService&svc,const Request&req,const User&currentUser,const json&data)
{
    User u;
    u.username=data.at("username").get<std::string>();
    u.password=data.at("password").get<std::string>();
    std::string roleStr=data.at("role").get<std::string>();
    if(roleStr=="admin"||roleStr=="ADMIN")
        u.role=UserRole::Admin;
    else
        u.role=UserRole::User;

    int id=svc.createUser(u,currentUser);

    json d;
    d["userId"]=id;
    return makeSuccess(req,d);
}

Response doListUsers(UserService&svc,const Request&req,const User&currentUser)
{
    auto users=svc.listUsers(currentUser);
    json arr=json::array();
    for(const auto&u:users)
    {
        json j;
        j["id"]=u.id;
        j["username"]=u.username;
        j["role"]=roleToString(u.role);
        arr.push_back(j);
    }
    json d;
    d["users"]=arr;
    return makeSuccess(req,d);
}

Response doResetPassword(UserService&svc,const Request&req,const User&currentUser,const json&data)
{
    int userId=data.at("userId").get<int>();
    std::string newPwd=data.at("newPassword").get<std::string>();

    bool ok=svc.resetPassword(userId,newPwd,currentUser);
    if(!ok)
        return makeError(req,"NOT_FOUND","User not found");

    json d; // 空对象
    return makeSuccess(req,d);
}

Response doCreateMaterial(MaterialService&svc,const Request&req,const User&currentUser,const json&data)
{
    std::string name=data.at("name").get<std::string>();
    std::string spec=data.at("spec").get<std::string>();
    std::string unit=data.at("unit").get<std::string>();

    int id=svc.createMaterial(name,spec,unit,currentUser);

    json d;
    d["materialId"]=id;
    return makeSuccess(req,d);
}

Response doListMaterials(MaterialService&svc,const Request&req)
{
    auto mats=svc.listMaterials();
    json arr=json::array();
    for(const auto&m:mats)
    {
        json j;
        j["id"]=m.id;
        j["name"]=m.name;
        j["spec"]=m.spec;
        j["unit"]=m.unit;
        arr.push_back(j);
    }
    json d;
    d["materials"]=arr;
    return makeSuccess(req,d);
}

Response doCreateSupplier(SupplierService&svc,const Request&req,const User&currentUser,const json&data)
{
    std::string name=data.at("name").get<std::string>();
    std::string contact=data.at("contact").get<std::string>();
    std::string phone=data.at("phone").get<std::string>();

    int id=svc.createSupplier(name,contact,phone,currentUser);

    json d;
    d["supplierId"]=id;
    return makeSuccess(req,d);
}

Response doListSuppliers(SupplierService&svc,const Request&req)
{
    auto sups=svc.listSuppliers();
    json arr=json::array();
    for(const auto&s:sups)
    {
        json j;
        j["id"]=s.id;
        j["name"]=s.name;
        j["contact"]=s.contact;
        j["phone"]=s.phone;
        arr.push_back(j);
    }
    json d;
    d["suppliers"]=arr;
    return makeSuccess(req,d);
}

Response doCreateOrder(OrderService&svc,const Request&req,const User&currentUser,const json&data)
{
    int supplierId=data.at("supplierId").get<int>();
    std::string date=data.at("date").get<std::string>();

    int id=svc.createOrder(supplierId,date,currentUser);

    json d;
    d["orderId"]=id;
    return makeSuccess(req,d);
}

Response doAddOrderItem(OrderService&svc,const Request&req,const User&currentUser,const json&data)
{
    int orderId=data.at("orderId").get<int>();
    int materialId=data.at("materialId").get<int>();
    int quantity=data.at("quantity").get<int>();
    double price=data.at("price").get<double>();

    int itemId=svc.addOrderItem(orderId,materialId,quantity,price,currentUser);

    json d;
    d["orderItemId"]=itemId;
    return makeSuccess(req,d);
}

Response doListOrders(OrderService&svc,const Request&req)
{
    auto list=svc.listOrders();
    json arr=json::array();
    for(const auto&s:list)
    {
        json j;
        j["id"]=s.order.id;
        j["supplierId"]=s.order.supplierId;
        j["supplierName"]=s.supplierName;
        j["date"]=s.order.date;
        j["status"]=statusToString(s.order.status);
        arr.push_back(j);
    }
    json d;
    d["orders"]=arr;
    return makeSuccess(req,d);
}

Response doGetOrderDetail(OrderService&svc,const Request&req,const json&data)
{
    int orderId=data.at("orderId").get<int>();
    auto opt=svc.getOrderDetail(orderId);
    if(!opt.has_value())
        return makeError(req,"NOT_FOUND","Order not found");

    auto detail=opt.value();
    json d;
    d["id"]=detail.order.id;
    d["supplierId"]=detail.order.supplierId;
    d["supplierName"]=detail.supplierName;
    d["date"]=detail.order.date;
    d["status"]=statusToString(detail.order.status);

    json arr=json::array();
    for(const auto&it:detail.items)
    {
        json j;
        j["id"]=it.id;
        j["orderId"]=it.orderId;
        j["materialId"]=it.materialId;
        j["quantity"]=it.quantity;
        j["price"]=it.price;
        j["receivedQuantity"]=it.receivedQuantity;
        arr.push_back(j);
    }
    d["items"]=arr;
    return makeSuccess(req,d);
}

Response doListOrdersBySupplier(OrderService&svc,const Request&req,const json&data)
{
    int supplierId=data.at("supplierId").get<int>();
    auto list=svc.listOrdersBySupplier(supplierId);

    json arr=json::array();
    for(const auto&s:list)
    {
        json j;
        j["id"]=s.order.id;
        j["supplierId"]=s.order.supplierId;
        j["supplierName"]=s.supplierName;
        j["date"]=s.order.date;
        j["status"]=statusToString(s.order.status);
        arr.push_back(j);
    }
    json d;
    d["orders"]=arr;
    return makeSuccess(req,d);
}

Response doListOpenOrders(OrderService&svc,const Request&req)
{
    auto list=svc.listOpenOrders();
    json arr=json::array();
    for(const auto&s:list)
    {
        json j;
        j["id"]=s.order.id;
        j["supplierId"]=s.order.supplierId;
        j["supplierName"]=s.supplierName;
        j["date"]=s.order.date;
        j["status"]=statusToString(s.order.status);
        arr.push_back(j);
    }
    json d;
    d["orders"]=arr;
    return makeSuccess(req,d);
}

Response doReceiveGoods(InventoryService&invSvc,const Request&req,const User&currentUser,const json&data)
{
    int orderId=data.at("orderId").get<int>();
    int itemId=data.at("orderItemId").get<int>(); // 前端字段叫 orderItemId
    int quantity=data.at("quantity").get<int>();

    bool orderClosed=false;
    int newRecv=0;
    int newStock=0;
    bool ok=invSvc.receiveGoods(orderId,itemId,quantity,currentUser,
                                orderClosed,newRecv,newStock);
    if(!ok)
        return makeError(req,"VALIDATION_ERROR","Invalid receiveGoods request");

    json d;
    d["orderClosed"]=orderClosed;
    d["receivedQuantity"]=newRecv;
    d["currentStock"]=newStock;
    return makeSuccess(req,d);
}

Response doGetStock(InventoryService&svc,const Request&req,const json&data)
{
    int materialId=data.at("materialId").get<int>();
    int qty=svc.getStock(materialId);

    json d;
    d["materialId"]=materialId;
    d["quantity"]=qty;
    return makeSuccess(req,d);
}

Response doListStocks(InventoryService&svc,const Request&req)
{
    auto list=svc.listStocks();
    json arr=json::array();
    for(const auto&s:list)
    {
        json j;
        j["materialId"]=s.materialId;
        j["quantity"]=s.quantity;
        arr.push_back(j);
    }
    json d;
    d["stocks"]=arr;
    return makeSuccess(req,d);
}

} // anonymous namespace

// ======================
// RequestDispatcher 成员
// ======================

RequestDispatcher::RequestDispatcher(AuthService&authSvc,
                                     UserService&userSvc,
                                     MaterialService&matSvc,
                                     SupplierService&supSvc,
                                     OrderService&ordSvc,
                                     InventoryService&invSvc)
    :authService(authSvc),
     userService(userSvc),
     materialService(matSvc),
     supplierService(supSvc),
     orderService(ordSvc),
     inventoryService(invSvc)
{
}

std::string RequestDispatcher::handleRequest(const std::string&requestJson)
{
    Response resp;

    try
    {
        json root=json::parse(requestJson);

        Request req;
        req.requestId=root.value("requestId",std::string());
        req.action=root.value("action",std::string());
        req.token="";
        if(root.contains("auth")&&root["auth"].is_object())
            req.token=root["auth"].value("token",std::string());

        json data=json::object();
        if(root.contains("data"))
            data=root["data"];
        req.rawData=data.dump();

        // 没有 action 直接报错
        if(req.action.empty())
        {
            resp=makeError(req,"INVALID_REQUEST","Missing action");
        }
        else if(req.action=="Login")
        {
            // Login 不需要 token
            try
            {
                resp=doLogin(authService,req,data);
            }
            catch(const std::exception&e)
            {
                resp=makeError(req,"VALIDATION_ERROR",e.what());
            }
        }
        else
        {
            // 其他操作统一先鉴权
            auto optUser=authService.getUserByToken(req.token);
            if(!optUser.has_value())
            {
                resp=makeError(req,"NOT_AUTHORIZED","Invalid or missing token");
            }
            else
            {
                User currentUser=optUser.value();
                try
                {
                    const std::string&act=req.action;

                    if(act=="CreateUser")
                        resp=doCreateUser(userService,req,currentUser,data);
                    else if(act=="ListUsers")
                        resp=doListUsers(userService,req,currentUser);
                    else if(act=="ResetPassword")
                        resp=doResetPassword(userService,req,currentUser,data);

                    else if(act=="CreateMaterial")
                        resp=doCreateMaterial(materialService,req,currentUser,data);
                    else if(act=="ListMaterials")
                        resp=doListMaterials(materialService,req);

                    else if(act=="CreateSupplier")
                        resp=doCreateSupplier(supplierService,req,currentUser,data);
                    else if(act=="ListSuppliers")
                        resp=doListSuppliers(supplierService,req);

                    else if(act=="CreateOrder")
                        resp=doCreateOrder(orderService,req,currentUser,data);
                    else if(act=="AddOrderItem")
                        resp=doAddOrderItem(orderService,req,currentUser,data);
                    else if(act=="ListOrders")
                        resp=doListOrders(orderService,req);
                    else if(act=="GetOrderDetail")
                        resp=doGetOrderDetail(orderService,req,data);
                    else if(act=="ListOrdersBySupplier")
                        resp=doListOrdersBySupplier(orderService,req,data);
                    else if(act=="ListOpenOrders")
                        resp=doListOpenOrders(orderService,req);

                    else if(act=="ReceiveGoods")
                        resp=doReceiveGoods(inventoryService,req,currentUser,data);
                    else if(act=="GetStock")
                        resp=doGetStock(inventoryService,req,data);
                    else if(act=="ListStocks")
                        resp=doListStocks(inventoryService,req);

                    else
                        resp=makeError(req,"UNKNOWN_ACTION","Unknown action");
                }
                catch(const std::exception&e)
                {
                    resp=makeError(req,"VALIDATION_ERROR",e.what());
                }
            }
        }
    }
    catch(const json::parse_error&)
    {
        resp=makeErrorNoReq("INVALID_REQUEST","Invalid JSON");
    }
    catch(const std::exception&e)
    {
        std::cerr<<"[Dispatcher] unexpected error: "<<e.what()<<"\n";
        resp=makeErrorNoReq("INTERNAL_ERROR","Internal server error");
    }

    // encode Response -> JSON 字符串
    json out;
    out["requestId"]=resp.requestId;
    out["success"]=resp.success;
    if(!resp.errorCode.empty())out["errorCode"]=resp.errorCode;
    if(!resp.message.empty())out["message"]=resp.message;

    json data=json::object();
    if(!resp.dataJson.empty())
    {
        try
        {
            data=json::parse(resp.dataJson);
        }
        catch(...)
        {
            data=json::object();
        }
    }
    out["data"]=data;

    return out.dump(); // 不带 '\n'，由 TcpServer 负责加行尾
}
