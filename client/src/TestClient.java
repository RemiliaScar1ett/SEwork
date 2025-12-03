import net.TcpClient;
import protocol.ApiClient;
import protocol.ApiException;
import service.*;
import model.*;

import java.io.IOException;
import java.util.List;

/**
 * 冒烟测试：不涉及 UI，只测试 net + protocol + model + service (+ 后端全栈)。
 */
public class TestClient {

    public static void main(String[] args){
        System.out.println("=== ClientSmokeTest start ===");

        TcpClient tcp=null;
        try{
            // 1. 建立 TCP 连接
            tcp=new TcpClient("127.0.0.1",5000);
            tcp.connect();
            System.out.println("[OK] Connected to server.");

            // 2. 构造 ApiClient 和各 Service
            ApiClient apiClient=new ApiClient(tcp);
            AuthClientService authService=new AuthClientService(apiClient);
            UserClientService userService=new UserClientService(apiClient,authService);
            MaterialClientService materialService=new MaterialClientService(apiClient,authService);
            SupplierClientService supplierService=new SupplierClientService(apiClient,authService);
            OrderClientService orderService=new OrderClientService(apiClient,authService);
            InventoryClientService inventoryService=new InventoryClientService(apiClient,authService);

            // 3. 登录（假设后端有 admin / 123456）
            if(!authService.login("admin","123456")){
                System.err.println("[FAIL] Login failed: "+authService.getLastErrorMessage());
                return;
            }
            AuthSession session=authService.getSession();
            System.out.println("[OK] Login as "+session.username+" (role="+session.role+") token="+session.token);

            // 4. 创建供应商
            int supplierId=supplierService.createSupplier("测试供应商A","张三","13800000000");
            System.out.println("[OK] Created supplier, id="+supplierId);

            // 5. 创建物资
            int materialId=materialService.createMaterial("测试物资X","规格XYZ","件");
            System.out.println("[OK] Created material, id="+materialId);

            // 6. 创建订单
            String today="2025-12-04"; // 可以随便写一个日期字符串
            int orderId=orderService.createOrder(supplierId,today);
            System.out.println("[OK] Created order, id="+orderId);

            // 7. 添加订单明细
            int quantity=100;
            double price=12.5;
            int itemId=orderService.addOrderItem(orderId,materialId,quantity,price);
            System.out.println("[OK] Added order item, id="+itemId
                               +", orderId="+orderId+", materialId="+materialId);

            // 8. 查询订单列表
            List<OrderSummaryDto> orders=orderService.listOrders();
            System.out.println("[INFO] Current orders:");
            for(OrderSummaryDto o:orders){
                System.out.println("  - id="+o.id+", supplierId="+o.supplierId
                        +", supplierName="+o.supplierName
                        +", date="+o.date+", status="+o.status);
            }

            // 9. 查询订单详情
            OrderDetailDto detail=orderService.getOrderDetail(orderId);
            System.out.println("[INFO] Order detail for id="+orderId+": supplierName="
                               +detail.supplierName+", status="+detail.status);
            if(detail.items!=null){
                for(OrderItemDto it:detail.items){
                    System.out.println("  item id="+it.id
                            +", materialId="+it.materialId
                            +", qty="+it.quantity
                            +", received="+it.receivedQuantity
                            +", price="+it.price);
                }
            }

            // 10. 收货（收 30 个）
            int recvQty=30;
            inventoryService.receiveGoods(orderId,itemId,recvQty);
            System.out.println("[OK] ReceiveGoods: orderId="+orderId
                               +", itemId="+itemId+", quantity="+recvQty);

            // 11. 再查一次订单详情看收货数量变化
            OrderDetailDto detail2=orderService.getOrderDetail(orderId);
            System.out.println("[INFO] After receive, order status="+detail2.status);
            if(detail2.items!=null){
                for(OrderItemDto it:detail2.items){
                    System.out.println("  item id="+it.id
                            +", qty="+it.quantity
                            +", received="+it.receivedQuantity);
                }
            }

            // 12. 查看库存列表
            List<StockDto> stocks=inventoryService.listStocks();
            System.out.println("[INFO] Current stocks:");
            for(StockDto s:stocks){
                System.out.println("  materialId="+s.materialId+", quantity="+s.quantity);
            }

            // 13. 查询该物资的单个库存
            int stockQty=inventoryService.getStock(materialId);
            System.out.println("[INFO] Stock for material "+materialId+" = "+stockQty);

            System.out.println("=== ClientSmokeTest success ===");
        }
        catch(ApiException e){
            System.err.println("[API ERROR] code="+e.getErrorCode()+", message="+e.getMessage());
            e.printStackTrace();
        }
        catch(IOException e){
            System.err.println("[IO ERROR] "+e.getMessage());
            e.printStackTrace();
        }
        catch(Exception e){
            System.err.println("[UNEXPECTED ERROR] "+e.getMessage());
            e.printStackTrace();
        }
        finally{
            if(tcp!=null) tcp.close();
        }
    }
}
