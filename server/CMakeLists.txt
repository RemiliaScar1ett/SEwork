cmake_minimum_required(VERSION 3.15)
project(PurchaseServer)

# 使用 C++17
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

if(MSVC)
    add_compile_options(/utf-8)
endif()

# 头文件路径
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)

# 源文件列表（你有哪个就写哪个，没有的可以先删掉）
set(SERVER_SOURCES
    src/main.cpp

    src/net/TcpServer.cpp

    src/protocol/RequestDispatcher.cpp
    # src/protocol/Request.cpp
    # src/protocol/Response.cpp

    src/service/AuthService.cpp
    src/service/UserService.cpp
    src/service/MaterialService.cpp
    src/service/SupplierService.cpp
    src/service/OrderService.cpp
    src/service/InventoryService.cpp

    src/repository/UserRepository.cpp
    src/repository/MaterialRepository.cpp
    src/repository/SupplierRepository.cpp
    src/repository/OrderRepository.cpp
    src/repository/InventoryRepository.cpp

    src/persistence/DataStorage.cpp
)

add_executable(PurchaseServer ${SERVER_SOURCES})

# Windows 下链接 ws2_32
if(WIN32)
    target_compile_definitions(PurchaseServer PRIVATE _WIN32_WINNT=0x0601 WIN32_LEAN_AND_MEAN)
    target_link_libraries(PurchaseServer ws2_32)
endif()

# 可以按目录分组（在 VS 里显示好看，可选）
source_group("Source Files" FILES src/main.cpp)
source_group("Source Files\\net" FILES src/net/TcpServer.cpp)
source_group("Source Files\\protocol" FILES src/protocol/RequestDispatcher.cpp)
source_group("Source Files\\service" FILES
    src/service/AuthService.cpp
    src/service/UserService.cpp
    src/service/MaterialService.cpp
    src/service/SupplierService.cpp
    src/service/OrderService.cpp
    src/service/InventoryService.cpp
)
source_group("Source Files\\repository" FILES
    src/repository/UserRepository.cpp
    src/repository/MaterialRepository.cpp
    src/repository/SupplierRepository.cpp
    src/repository/OrderRepository.cpp
    src/repository/InventoryRepository.cpp
)

# # Service 层测试
# add_executable(PurchaseServerTests
#     tests/ServiceTest.cpp
#     src/service/AuthService.cpp
#     src/service/UserService.cpp
#     src/service/MaterialService.cpp
#     src/service/SupplierService.cpp
#     src/service/OrderService.cpp
#     src/service/InventoryService.cpp
# )
