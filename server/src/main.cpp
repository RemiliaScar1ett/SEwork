// server/src/main.cpp
#include <iostream>
#include <chrono>
#include "net/TcpServer.h"
#include "protocol/RequestDispatcher.h"
#include "persistence/DataStorage.h"

int main()
{
    std::cout<<"Starting PurchaseServer...\n";

    // 1. 数据层：加载 CSV
    DataStorage storage("data");
    storage.loadAll();

    // 如果第一次运行，没有 admin 用户，可以在这里初始化一个默认 admin
    if(!storage.userRepo.findByUsername("admin").has_value())
    {
        User admin;
        admin.username="admin";
        admin.password="123456";
        admin.role=UserRole::Admin;
        storage.userRepo.addUser(admin);
        std::cout<<"Initialized default admin user: admin / 123456\n";
    }

    // 2. 构造 Service 层
    AuthService authService(storage.userRepo);
    UserService userService(storage.userRepo);
    MaterialService materialService(storage.materialRepo);
    SupplierService supplierService(storage.supplierRepo);
    OrderService orderService(storage.orderRepo,storage.supplierRepo,storage.materialRepo);
    InventoryService inventoryService(storage.inventoryRepo,storage.orderRepo);

    // 3. 构造 Dispatcher
    RequestDispatcher dispatcher(authService,userService,
                                 materialService,supplierService,
                                 orderService,inventoryService);




    // 4. 启动 TCP 服务器
    TcpServer server(5000,dispatcher);

    using clock=std::chrono::steady_clock;
    auto lastSave=clock::now();
    auto interval=std::chrono::seconds(30);
    server.setTickCallback([&storage,&lastSave,&interval](){
        auto now=clock::now();
        if(now-lastSave>=interval)
        {
            storage.saveAll();
            lastSave=now;
            std::cout<<"[Autosave] data saved.\n";
        }
    });

    server.start();

    // 5. 服务器结束后保存数据
    storage.saveAll();

    return 0;
}
