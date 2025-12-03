package ui;

import service.InventoryClientService;
import service.MaterialClientService;

import javax.swing.*;
import java.awt.*;

/**
 * 库存查询面板（占位版本）。
 */
public class InventoryPanel extends JPanel {
    private final InventoryClientService inventoryService;
    private final MaterialClientService materialService;

    public InventoryPanel(InventoryClientService inventoryService,
                          MaterialClientService materialService){
        this.inventoryService=inventoryService;
        this.materialService=materialService;
        initUi();
    }

    private void initUi(){
        setLayout(new BorderLayout());
        add(new JLabel("库存查询功能（待实现）",SwingConstants.CENTER),BorderLayout.CENTER);
    }
}
