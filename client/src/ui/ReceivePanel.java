package ui;

import model.OrderDetailDto;
import model.OrderItemDto;
import model.OrderSummaryDto;
import protocol.ApiException;
import service.InventoryClientService;
import service.OrderClientService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * 收货管理面板：
 * - 左侧列出所有未完成订单（status != CLOSED）
 * - 右上显示选中订单的明细
 * - 右下输入本次收货数量，对选中明细执行收货
 */
public class ReceivePanel extends JPanel implements RefreshablePanel{
    private final OrderClientService orderService;
    private final InventoryClientService inventoryService;

    // 未完成订单列表
    private JTable orderTable;
    private DefaultTableModel orderTableModel;

    // 当前订单明细列表
    private JTable itemTable;
    private DefaultTableModel itemTableModel;

    // 状态显示
    private JLabel orderInfoLabel;
    private JLabel itemInfoLabel;
    private JLabel stockLabel;

    // 收货输入
    private JTextField receiveQtyField;

    // 当前选中
    private int currentOrderId=-1;
    private int currentItemId=-1;
    private int currentItemMaterialId=-1;

    public ReceivePanel(OrderClientService orderService,
                        InventoryClientService inventoryService){
        this.orderService=orderService;
        this.inventoryService=inventoryService;

        initUi();
        loadOpenOrders();
    }

    private void initUi(){
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // 左侧：未完成订单
        JPanel leftPanel=createOrderListPanel();

        // 右侧：订单明细 + 收货表单
        JPanel rightPanel=createRightPanel();

        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,leftPanel,rightPanel);
        split.setResizeWeight(0.4);

        add(split,BorderLayout.CENTER);
    }

    // ================== 左侧：未完成订单列表 ==================

    private JPanel createOrderListPanel(){
        JPanel panel=new JPanel(new BorderLayout(5,5));
        panel.setBorder(BorderFactory.createTitledBorder("未完成订单"));

        JButton refreshBtn=new JButton("刷新未完成订单");
        refreshBtn.addActionListener(e->loadOpenOrders());

        JPanel topBar=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        topBar.add(refreshBtn);

        String[]columns={"ID","供应商","日期","状态"};
        orderTableModel=new DefaultTableModel(columns,0){
            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        orderTable=new JTable(orderTableModel);
        JScrollPane scrollPane=new JScrollPane(orderTable);

        orderTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e){
                if(e.getValueIsAdjusting()) return;
                onOrderSelected();
            }
        });

        panel.add(topBar,BorderLayout.NORTH);
        panel.add(scrollPane,BorderLayout.CENTER);
        return panel;
    }

    private void loadOpenOrders(){
        try{
            List<OrderSummaryDto> list=orderService.listOpenOrders();
            orderTableModel.setRowCount(0);
            for(OrderSummaryDto o:list){
                Object[]row={o.id,o.supplierName,o.date,o.status};
                orderTableModel.addRow(row);
            }
            currentOrderId=-1;
            currentItemId=-1;
            currentItemMaterialId=-1;
            itemTableModel.setRowCount(0);
            orderInfoLabel.setText("当前未选中订单");
            itemInfoLabel.setText("当前未选中明细");
            stockLabel.setText("当前库存: -");
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "加载未完成订单失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onOrderSelected(){
        int row=orderTable.getSelectedRow();
        if(row<0){
            currentOrderId=-1;
            itemTableModel.setRowCount(0);
            orderInfoLabel.setText("当前未选中订单");
            itemInfoLabel.setText("当前未选中明细");
            stockLabel.setText("当前库存: -");
            return;
        }

        int modelRow=orderTable.convertRowIndexToModel(row);
        int orderId=(int)orderTableModel.getValueAt(modelRow,0);
        loadOrderDetail(orderId);
    }

    // ================== 右侧：明细 + 收货 ==================

    private JPanel createRightPanel(){
        JPanel panel=new JPanel(new BorderLayout(5,5));

        // 上：明细表
        JPanel detailPanel=new JPanel(new BorderLayout(5,5));
        detailPanel.setBorder(BorderFactory.createTitledBorder("订单明细"));

        orderInfoLabel=new JLabel("当前未选中订单");
        String[]cols={"明细ID","物资ID","数量","已收货","单价"};
        itemTableModel=new DefaultTableModel(cols,0){
            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        itemTable=new JTable(itemTableModel);
        JScrollPane scrollPane=new JScrollPane(itemTable);

        itemTable.getSelectionModel().addListSelectionListener(e->{
            if(e.getValueIsAdjusting()) return;
            onItemSelected();
        });

        detailPanel.add(orderInfoLabel,BorderLayout.NORTH);
        detailPanel.add(scrollPane,BorderLayout.CENTER);

        // 下：收货表单
        JPanel receivePanel=createReceiveFormPanel();

        panel.add(detailPanel,BorderLayout.CENTER);
        panel.add(receivePanel,BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReceiveFormPanel(){
        JPanel panel=new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("执行收货"));

        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(5,5,5,5);
        gbc.anchor=GridBagConstraints.WEST;

        itemInfoLabel=new JLabel("当前未选中明细");
        stockLabel=new JLabel("当前库存: -");

        JLabel qtyLabel=new JLabel("本次收货数量:");
        receiveQtyField=new JTextField(8);

        JButton receiveBtn=new JButton("执行收货");
        receiveBtn.addActionListener(e->onReceive());

        int y=0;

        gbc.gridx=0;gbc.gridy=y;gbc.gridwidth=2;
        panel.add(itemInfoLabel,gbc);y++;

        gbc.gridx=0;gbc.gridy=y;gbc.gridwidth=2;
        panel.add(stockLabel,gbc);y++;

        gbc.gridx=0;gbc.gridy=y;gbc.gridwidth=1;
        panel.add(qtyLabel,gbc);
        gbc.gridx=1;
        panel.add(receiveQtyField,gbc);
        y++;

        gbc.gridx=0;gbc.gridy=y;gbc.gridwidth=2;
        gbc.anchor=GridBagConstraints.CENTER;
        panel.add(receiveBtn,gbc);

        return panel;
    }

    private void loadOrderDetail(int orderId){
        try{
            OrderDetailDto detail=orderService.getOrderDetail(orderId);
            currentOrderId=detail.id;

            String label="订单ID="+detail.id
                         +" | 供应商="+detail.supplierName
                         +" | 日期="+detail.date
                         +" | 状态="+detail.status;
            orderInfoLabel.setText(label);

            itemTableModel.setRowCount(0);
            if(detail.items!=null){
                for(OrderItemDto it:detail.items){
                    Object[]row={it.id,it.materialId,it.quantity,it.receivedQuantity,it.price};
                    itemTableModel.addRow(row);
                }
            }

            currentItemId=-1;
            currentItemMaterialId=-1;
            itemInfoLabel.setText("当前未选中明细");
            stockLabel.setText("当前库存: -");
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "加载订单详情失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onItemSelected(){
        int row=itemTable.getSelectedRow();
        if(row<0){
            currentItemId=-1;
            currentItemMaterialId=-1;
            itemInfoLabel.setText("当前未选中明细");
            stockLabel.setText("当前库存: -");
            return;
        }
        int modelRow=itemTable.convertRowIndexToModel(row);
        int itemId=(int)itemTableModel.getValueAt(modelRow,0);
        int materialId=(int)itemTableModel.getValueAt(modelRow,1);
        int qty=(int)itemTableModel.getValueAt(modelRow,2);
        int received=(int)itemTableModel.getValueAt(modelRow,3);

        currentItemId=itemId;
        currentItemMaterialId=materialId;

        itemInfoLabel.setText("明细ID="+itemId
                              +" | 物资ID="+materialId
                              +" | 计划="+qty
                              +" | 已收="+received);

        // 查询当前库存
        try{
            int stock=inventoryService.getStock(materialId);
            stockLabel.setText("当前库存: "+stock);
        }catch(ApiException ex){
            stockLabel.setText("当前库存: 查询失败("+ex.getMessage()+")");
        }catch(IOException ex){
            stockLabel.setText("当前库存: 网络错误("+ex.getMessage()+")");
        }
    }

    private void onReceive(){
        if(currentOrderId<=0){
            JOptionPane.showMessageDialog(this,"请先在左侧选择一个订单",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(currentItemId<=0){
            JOptionPane.showMessageDialog(this,"请先在右侧选择一条明细",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qtyText=receiveQtyField.getText().trim();
        int qty;
        try{
            qty=Integer.parseInt(qtyText);
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this,"收货数量必须是整数",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(qty<=0){
            JOptionPane.showMessageDialog(this,"收货数量必须大于 0",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }

        try{
            inventoryService.receiveGoods(currentOrderId,currentItemId,qty);
            JOptionPane.showMessageDialog(this,
                    "收货操作成功",
                    "成功",JOptionPane.INFORMATION_MESSAGE);

            receiveQtyField.setText("");

            // 收货后刷新当前订单详情和库存
            loadOrderDetail(currentOrderId);
            if(currentItemMaterialId>0){
                try{
                    int stock=inventoryService.getStock(currentItemMaterialId);
                    stockLabel.setText("当前库存: "+stock);
                }catch(Exception ignored){}
            }

            // 同时刷新左侧未完成订单列表（有可能订单已经 Closed）
            loadOpenOrders();
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "收货失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }
    @Override
    public void reloadData(){
        loadOpenOrders();
    }
}
