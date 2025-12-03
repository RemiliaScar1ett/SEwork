package ui;

import net.TcpClient;
import service.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 主窗口：包含各个功能 Tab 面板。
 */
public class MainFrame extends JFrame {
    private final TcpClient tcpClient;
    private final AuthClientService authService;
    private final UserClientService userService;
    private final MaterialClientService materialService;
    private final SupplierClientService supplierService;
    private final OrderClientService orderService;
    private final InventoryClientService inventoryService;

    private JTabbedPane tabbedPane;

    public MainFrame(TcpClient tcpClient,
                     AuthClientService authService,
                     UserClientService userService,
                     MaterialClientService materialService,
                     SupplierClientService supplierService,
                     OrderClientService orderService,
                     InventoryClientService inventoryService){
        this.tcpClient=tcpClient;
        this.authService=authService;
        this.userService=userService;
        this.materialService=materialService;
        this.supplierService=supplierService;
        this.orderService=orderService;
        this.inventoryService=inventoryService;

        initUi();
    }

    private void initUi(){
        String username=authService.getSession().username;
        String role=authService.getSession().role;
        setTitle("物资采购系统 - 当前用户: "+username+" ("+role+")");

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900,600);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                int opt=JOptionPane.showConfirmDialog(MainFrame.this,
                        "确定要退出系统吗？","确认退出",
                        JOptionPane.YES_NO_OPTION);
                if(opt==JOptionPane.YES_OPTION){
                    tcpClient.close();
                    dispose();
                    System.exit(0);
                }
            }
        });

        tabbedPane=new JTabbedPane();

        // 用户管理（仅 admin 显示）
        if(authService.isAdmin()){
            UserPanel userPanel=new UserPanel(userService);
            tabbedPane.addTab("用户管理",userPanel);
        }

        MaterialPanel materialPanel=new MaterialPanel(materialService);
        SupplierPanel supplierPanel=new SupplierPanel(supplierService);
        OrderPanel orderPanel=new OrderPanel(orderService,materialService,supplierService);
        ReceivePanel receivePanel=new ReceivePanel(orderService,inventoryService);
        InventoryPanel inventoryPanel=new InventoryPanel(inventoryService,materialService);

        tabbedPane.addTab("物资管理",materialPanel);
        tabbedPane.addTab("供应商管理",supplierPanel);
        tabbedPane.addTab("订单管理",orderPanel);
        tabbedPane.addTab("收货管理",receivePanel);
        tabbedPane.addTab("库存查询",inventoryPanel);

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e){
                Component c=tabbedPane.getSelectedComponent();
                if(c instanceof RefreshablePanel){
                    ((RefreshablePanel)c).reloadData();
                }
            }
        });
        
        Component c=tabbedPane.getSelectedComponent();
        if(c instanceof RefreshablePanel){
            ((RefreshablePanel)c).reloadData();
        }

        setLayout(new BorderLayout());
        add(tabbedPane,BorderLayout.CENTER);
    }
}
