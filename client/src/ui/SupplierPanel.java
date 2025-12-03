package ui;

import service.SupplierClientService;

import javax.swing.*;
import java.awt.*;

/**
 * 供应商管理面板（占位版本）。
 */
public class SupplierPanel extends JPanel {
    private final SupplierClientService supplierService;

    public SupplierPanel(SupplierClientService supplierService){
        this.supplierService=supplierService;
        initUi();
    }

    private void initUi(){
        setLayout(new BorderLayout());
        add(new JLabel("供应商管理功能（待实现）",SwingConstants.CENTER),BorderLayout.CENTER);
    }
}
