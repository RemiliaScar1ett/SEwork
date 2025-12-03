import net.TcpClient;
import protocol.ApiClient;
import service.*;
import ui.LoginFrame;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[]args){
        // 全局未捕获异常处理：任何线程有未捕获异常都会走这里
        Thread.setDefaultUncaughtExceptionHandler((t,e)->{
            e.printStackTrace(); // 如果用 java -jar 跑，仍然能在控制台看到栈
            SwingUtilities.invokeLater(()->{
                JOptionPane.showMessageDialog(
                        null,
                        "Unexpected error:\n"+e.toString(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            });
        });

        SwingUtilities.invokeLater(()->{
            TcpClient tcpClient=new TcpClient("127.0.0.1",5000);
            try{
                tcpClient.connect();
            }catch(IOException e){
                // 后端没启动 / 连接被拒绝 都会到这里
                e.printStackTrace(); // 命令行调试用
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to connect backend server:\n"+e.getMessage(),
                        "Connection error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1); // 不要继续跑 UI 了
            }

            ApiClient apiClient=new ApiClient(tcpClient);

            AuthClientService authService=new AuthClientService(apiClient);
            UserClientService userService=new UserClientService(apiClient,authService);
            MaterialClientService materialService=new MaterialClientService(apiClient,authService);
            SupplierClientService supplierService=new SupplierClientService(apiClient,authService);
            OrderClientService orderService=new OrderClientService(apiClient,authService);
            InventoryClientService inventoryService=new InventoryClientService(apiClient,authService);

            LoginFrame loginFrame=new LoginFrame(
                    tcpClient,
                    authService,
                    userService,
                    materialService,
                    supplierService,
                    orderService,
                    inventoryService
            );
            loginFrame.setVisible(true);
        });
    }
}
