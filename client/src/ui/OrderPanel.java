package ui;

import service.MaterialClientService;
import service.OrderClientService;
import service.SupplierClientService;

import javax.swing.*;
import java.awt.*;

/**
 * 订单管理面板（占位版本）。
 */
public class OrderPanel extends JPanel {
    private final OrderClientService orderService;
    private final MaterialClientService materialService;
    private final SupplierClientService supplierService;

    public OrderPanel(OrderClientService orderService,
                      MaterialClientService materialService,
                      SupplierClientService supplierService){
        this.orderService=orderService;
        this.materialService=materialService;
        this.supplierService=supplierService;
        initUi();
    }

    private void initUi(){
        setLayout(new BorderLayout());
        add(new JLabel("订单管理功能（待实现）",SwingConstants.CENTER),BorderLayout.CENTER);
    }
}
