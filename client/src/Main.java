import net.TcpClient;
import protocol.ApiClient;
import service.*;
import ui.LoginFrame;

import javax.swing.*;
import java.io.IOException;

/**
 * 程序入口：创建 TcpClient / ApiClient / 各种 service，然后启动登录窗口。
 */
public class Main {
    public static void main(String[]args){
        SwingUtilities.invokeLater(()->{
            TcpClient tcpClient=new TcpClient("127.0.0.1",5000);
            try{
                tcpClient.connect();
            }catch(IOException e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "无法连接后端服务器: "+e.getMessage(),
                        "错误",JOptionPane.ERROR_MESSAGE);
                return;
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
