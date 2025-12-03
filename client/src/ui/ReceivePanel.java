package ui;

import service.InventoryClientService;
import service.OrderClientService;

import javax.swing.*;
import java.awt.*;

/**
 * 收货管理面板（占位版本）。
 */
public class ReceivePanel extends JPanel {
    private final OrderClientService orderService;
    private final InventoryClientService inventoryService;

    public ReceivePanel(OrderClientService orderService,
                        InventoryClientService inventoryService){
        this.orderService=orderService;
        this.inventoryService=inventoryService;
        initUi();
    }

    private void initUi(){
        setLayout(new BorderLayout());
        add(new JLabel("收货管理功能（待实现）",SwingConstants.CENTER),BorderLayout.CENTER);
    }
}
