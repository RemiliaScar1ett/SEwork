// server/tests/ServiceTests.cpp
#include <iostream>
#include <vector>
#include <unordered_map>
#include <optional>
#include <stdexcept>
#include <string>

// ====== 引入 Service 和 Domain/Repo 头文件 ======
#include "domain/User.h"
#include "domain/Material.h"
#include "domain/Supplier.h"
#include "domain/PurchaseOrder.h"
#include "domain/InventoryRecord.h"

#include "repository/UserRepository.h"
#include "repository/MaterialRepository.h"
#include "repository/SupplierRepository.h"
#include "repository/OrderRepository.h"
#include "repository/InventoryRepository.h"

#include "service/AuthService.h"
#include "service/UserService.h"
#include "service/MaterialService.h"
#include "service/SupplierService.h"
#include "service/OrderService.h"
#include "service/InventoryService.h"

// ====== 简易测试工具 ======
int g_failed=0;

void checkImpl(bool cond,const char*expr,const char*file,int line)
{
    if(!cond)
    {
        std::cerr<<"[FAIL] "<<file<<":"<<line<<" : "<<expr<<"\n";
        g_failed++;
    }
}

#define CHECK(cond) checkImpl((cond),#cond,__FILE__,__LINE__)

// ======================
// 内存版 Repository 实现
// ======================

// 这些实现只在测试程序中链接，真正的服务器可以有自己的 Repository.cpp。
// 这里不实现 CSV，load/save 可以是空壳。

// ---------- UserRepository ----------
void UserRepository::loadFromCsv(const std::string&path)
{
    // 测试中不需要，从空集合开始
    (void)path;
    users.clear();
}

void UserRepository::saveToCsv(const std::string&path) const
{
    (void)path;
    // 测试中不写文件
}

std::optional<User> UserRepository::findById(int id) const
{
    for(const auto&u:users)
        if(u.id==id) return u;
    return std::nullopt;
}

std::optional<User> UserRepository::findByUsername(const std::string&username) const
{
    for(const auto&u:users)
        if(u.username==username) return u;
    return std::nullopt;
}

int UserRepository::addUser(const User&user)
{
    int id=nextId();
    User u=user;
    u.id=id;
    users.push_back(u);
    return id;
}

bool UserRepository::updateUser(const User&user)
{
    for(auto&u:users)
    {
        if(u.id==user.id)
        {
            u=user;
            return true;
        }
    }
    return false;
}

const std::vector<User>& UserRepository::getAll() const
{
    return users;
}

int UserRepository::nextId() const
{
    int maxId=0;
    for(const auto&u:users)
        if(u.id>maxId) maxId=u.id;
    return maxId+1;
}

// ---------- MaterialRepository ----------
void MaterialRepository::loadFromCsv(const std::string&path)
{
    (void)path;
    materials.clear();
}

void MaterialRepository::saveToCsv(const std::string&path) const
{
    (void)path;
}

std::optional<Material> MaterialRepository::findById(int id) const
{
    for(const auto&m:materials)
        if(m.id==id) return m;
    return std::nullopt;
}

int MaterialRepository::addMaterial(const Material&m)
{
    int id=nextId();
    Material copy=m;
    copy.id=id;
    materials.push_back(copy);
    return id;
}

bool MaterialRepository::updateMaterial(const Material&m)
{
    for(auto&x:materials)
    {
        if(x.id==m.id)
        {
            x=m;
            return true;
        }
    }
    return false;
}

const std::vector<Material>& MaterialRepository::getAll() const
{
    return materials;
}

int MaterialRepository::nextId() const
{
    int maxId=0;
    for(const auto&m:materials)
        if(m.id>maxId) maxId=m.id;
    return maxId+1;
}

// ---------- SupplierRepository ----------
void SupplierRepository::loadFromCsv(const std::string&path)
{
    (void)path;
    suppliers.clear();
}

void SupplierRepository::saveToCsv(const std::string&path) const
{
    (void)path;
}

std::optional<Supplier> SupplierRepository::findById(int id) const
{
    for(const auto&s:suppliers)
        if(s.id==id) return s;
    return std::nullopt;
}

int SupplierRepository::addSupplier(const Supplier&s)
{
    int id=nextId();
    Supplier copy=s;
    copy.id=id;
    suppliers.push_back(copy);
    return id;
}

bool SupplierRepository::updateSupplier(const Supplier&s)
{
    for(auto&x:suppliers)
    {
        if(x.id==s.id)
        {
            x=s;
            return true;
        }
    }
    return false;
}

const std::vector<Supplier>& SupplierRepository::getAll() const
{
    return suppliers;
}

int SupplierRepository::nextId() const
{
    int maxId=0;
    for(const auto&s:suppliers)
        if(s.id>maxId) maxId=s.id;
    return maxId+1;
}

// ---------- OrderRepository ----------
void OrderRepository::loadFromCsv(const std::string&orderPath,const std::string&itemPath)
{
    (void)orderPath;
    (void)itemPath;
    orders.clear();
    items.clear();
}

void OrderRepository::saveToCsv(const std::string&orderPath,const std::string&itemPath) const
{
    (void)orderPath;
    (void)itemPath;
}

std::optional<PurchaseOrder> OrderRepository::findOrderById(int id) const
{
    for(const auto&o:orders)
        if(o.id==id) return o;
    return std::nullopt;
}

std::vector<PurchaseOrder> OrderRepository::getAllOrders() const
{
    return orders;
}

int OrderRepository::addOrder(const PurchaseOrder&o)
{
    int id=nextOrderId();
    PurchaseOrder copy=o;
    copy.id=id;
    orders.push_back(copy);
    return id;
}

bool OrderRepository::updateOrder(const PurchaseOrder&o)
{
    for(auto&x:orders)
    {
        if(x.id==o.id)
        {
            x=o;
            return true;
        }
    }
    return false;
}

std::vector<PurchaseOrderItem> OrderRepository::findItemsByOrderId(int orderId) const
{
    std::vector<PurchaseOrderItem> result;
    for(const auto&it:items)
        if(it.orderId==orderId) result.push_back(it);
    return result;
}

std::optional<PurchaseOrderItem> OrderRepository::findItemById(int itemId) const
{
    for(const auto&it:items)
        if(it.id==itemId) return it;
    return std::nullopt;
}

int OrderRepository::addOrderItem(const PurchaseOrderItem&item)
{
    int id=nextOrderItemId();
    PurchaseOrderItem copy=item;
    copy.id=id;
    items.push_back(copy);
    return id;
}

bool OrderRepository::updateOrderItem(const PurchaseOrderItem&item)
{
    for(auto&x:items)
    {
        if(x.id==item.id)
        {
            x=item;
            return true;
        }
    }
    return false;
}

int OrderRepository::nextOrderId() const
{
    int maxId=0;
    for(const auto&o:orders)
        if(o.id>maxId) maxId=o.id;
    return maxId+1;
}

int OrderRepository::nextOrderItemId() const
{
    int maxId=0;
    for(const auto&it:items)
        if(it.id>maxId) maxId=it.id;
    return maxId+1;
}

// ---------- InventoryRepository ----------
void InventoryRepository::loadFromCsv(const std::string&path)
{
    (void)path;
    stock.clear();
}

void InventoryRepository::saveToCsv(const std::string&path) const
{
    (void)path;
}

int InventoryRepository::getQuantity(int materialId) const
{
    auto it=stock.find(materialId);
    if(it==stock.end()) return 0;
    return it->second;
}

void InventoryRepository::setQuantity(int materialId,int quantity)
{
    if(quantity<=0)
        stock.erase(materialId);
    else
        stock[materialId]=quantity;
}

const std::unordered_map<int,int>& InventoryRepository::getAll() const
{
    return stock;
}

// ======================
// Service 单元测试
// ======================

void runAuthTests()
{
    std::cout<<"== AuthService tests ==\n";

    UserRepository userRepo;
    // 准备一个 admin 用户
    User admin;
    admin.username="admin";
    admin.password="123456";
    admin.role=UserRole::Admin;
    int adminId=userRepo.addUser(admin);
    CHECK(adminId==1);

    AuthService auth(userRepo);

    // 登录成功
    auto ok=auth.login("admin","123456");
    CHECK(ok.success);
    CHECK(ok.token.size()>0);
    CHECK(ok.user.username=="admin");

    // 登录失败
    auto bad=auth.login("admin","wrong");
    CHECK(!bad.success);

    // token 查用户
    auto u=auth.getUserByToken(ok.token);
    CHECK(u.has_value());
    CHECK(u->username=="admin");
}

void runUserServiceTests()
{
    std::cout<<"== UserService tests ==\n";

    UserRepository userRepo;
    // 初始化一个 admin
    User admin;
    admin.username="admin";
    admin.password="123456";
    admin.role=UserRole::Admin;
    admin.id=userRepo.addUser(admin);

    UserService userService(userRepo);

    // 创建普通用户
    User u;
    u.username="user1";
    u.password="pwd";
    u.role=UserRole::User;
    int uid=userService.createUser(u,admin);
    CHECK(uid==2);

    auto list=userService.listUsers(admin);
    CHECK(list.size()==2);

    // 重置密码
    bool ok=userService.resetPassword(uid,"newpwd",admin);
    CHECK(ok);
    auto opt=userRepo.findById(uid);
    CHECK(opt.has_value());
    CHECK(opt->password=="newpwd");

    // 非 admin 调用应抛异常（这里只是简单验证一下）
    bool threw=false;
    try
    {
        userService.listUsers(u);
    }
    catch(const std::exception&)
    {
        threw=true;
    }
    CHECK(threw);
}

void runMaterialSupplierTests()
{
    std::cout<<"== Material & Supplier tests ==\n";

    MaterialRepository matRepo;
    SupplierRepository supRepo;

    UserRepository userRepo;
    User admin;
    admin.username="admin";
    admin.password="123";
    admin.role=UserRole::Admin;
    admin.id=userRepo.addUser(admin);

    MaterialService matService(matRepo);
    SupplierService supService(supRepo);

    int mid1=matService.createMaterial("SteelPlate","Q235","sheet",admin);
    int mid2=matService.createMaterial("Screw","M8*30","pcs",admin);
    CHECK(mid1==1);
    CHECK(mid2==2);

    auto mats=matService.listMaterials();
    CHECK(mats.size()==2);

    int sid=supService.createSupplier("XXSteel","Contact","13800000000",admin);
    CHECK(sid==1);
    auto sups=supService.listSuppliers();
    CHECK(sups.size()==1);
}

void runOrderInventoryTests()
{
    std::cout<<"== Order & Inventory tests ==\n";

    // 准备仓储
    MaterialRepository matRepo;
    SupplierRepository supRepo;
    OrderRepository orderRepo;
    InventoryRepository invRepo;

    // 准备数据
    Material m1; m1.name="SteelPlate";m1.spec="Q235";m1.unit="sheet";
    int mid=matRepo.addMaterial(m1);

    Supplier s1; s1.name="XXSteel";s1.contact="Contact";s1.phone="138";
    int sid=supRepo.addSupplier(s1);

    User admin; admin.id=1; admin.username="admin"; admin.password="123"; admin.role=UserRole::Admin;

    OrderService orderService(orderRepo,supRepo,matRepo);
    InventoryService invService(invRepo,orderRepo);

    // 创建订单
    int oid=orderService.createOrder(sid,"2025-12-03",admin);
    CHECK(oid==1);

    // 添加明细：订 100 张钢板
    int itemId=orderService.addOrderItem(oid,mid,100,3500.0,admin);
    CHECK(itemId==1);

    // 初始库存应为 0
    CHECK(invService.getStock(mid)==0);

    // 初始订单状态为 NEW
    auto detail0=orderService.getOrderDetail(oid);
    CHECK(detail0.has_value());
    CHECK(detail0->order.status==OrderStatus::New);

    // 收货 50
    bool orderClosed=false;
    int newRecvQty=0;
    int newStock=0;
    bool ok=invService.receiveGoods(oid,itemId,50,admin,orderClosed,newRecvQty,newStock);
    CHECK(ok);
    CHECK(newRecvQty==50);
    CHECK(newStock==50);
    CHECK(!orderClosed);
    CHECK(invService.getStock(mid)==50);

    // 此时订单状态应为 PARTIAL
    auto detail1=orderService.getOrderDetail(oid);
    CHECK(detail1.has_value());
    CHECK(detail1->order.status==OrderStatus::Partial);

    // 再收 50 → 收满
    ok=invService.receiveGoods(oid,itemId,50,admin,orderClosed,newRecvQty,newStock);
    CHECK(ok);
    CHECK(newRecvQty==100);
    CHECK(newStock==100);
    CHECK(orderClosed);

    auto detail2=orderService.getOrderDetail(oid);
    CHECK(detail2.has_value());
    CHECK(detail2->order.status==OrderStatus::Closed);

    // 尝试超收 → 应失败且不改变库存
    ok=invService.receiveGoods(oid,itemId,1,admin,orderClosed,newRecvQty,newStock);
    CHECK(!ok);
    CHECK(invService.getStock(mid)==100);
}

int main()
{
    runAuthTests();
    runUserServiceTests();
    runMaterialSupplierTests();
    runOrderInventoryTests();

    if(g_failed==0)
    {
        std::cout<<"ALL TESTS PASSED\n";
        return 0;
    }
    else
    {
        std::cout<<g_failed<<" TEST(S) FAILED\n";
        return 1;
    }
}
