package ui;

import model.MaterialDto;
import model.OrderDetailDto;
import model.OrderItemDto;
import model.OrderSummaryDto;
import model.SupplierDto;
import protocol.ApiException;
import service.MaterialClientService;
import service.OrderClientService;
import service.SupplierClientService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 订单管理面板：
 * - 上半部分：订单列表 + 筛选按钮
 * - 中间：当前选中订单详情（明细列表）
 * - 下半部分：创建订单 + 添加明细
 */
public class OrderPanel extends JPanel implements RefreshablePanel {
    private final OrderClientService orderService;
    private final MaterialClientService materialService;
    private final SupplierClientService supplierService;

    // 订单列表
    private JTable orderTable;
    private DefaultTableModel orderTableModel;

    // 订单详情
    private JTable itemTable;
    private DefaultTableModel itemTableModel;
    private JLabel orderDetailLabel;
    private int currentOrderId=-1;

    // 创建订单表单
    private JComboBox<ComboItem> createOrderSupplierCombo;
    private JTextField createOrderDateField;

    // 添加明细表单
    private JComboBox<ComboItem> addItemMaterialCombo;
    private JTextField addItemQuantityField;
    private JTextField addItemPriceField;

    // 订单筛选：按供应商
    private JComboBox<ComboItem> filterSupplierCombo;

    public OrderPanel(OrderClientService orderService,
                      MaterialClientService materialService,
                      SupplierClientService supplierService){
        this.orderService=orderService;
        this.materialService=materialService;
        this.supplierService=supplierService;

        initUi();
        loadSuppliersForCombos();
        loadMaterialsForCombo();
        loadAllOrders();
    }

    private void initUi(){
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // ===== 上半部分：订单列表 + 筛选 =====
        JPanel ordersPanel=createOrdersPanel();

        // ===== 中间：订单详情 =====
        JPanel detailPanel=createDetailPanel();

        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,ordersPanel,detailPanel);
        split.setResizeWeight(0.5);

        // ===== 下半部分：创建订单 + 添加明细 =====
        JPanel formPanel=createFormPanel();

        add(split,BorderLayout.CENTER);
        add(formPanel,BorderLayout.SOUTH);
    }

    // ================== 创建各部分 UI ==================

    private JPanel createOrdersPanel(){
        JPanel panel=new JPanel(new BorderLayout(5,5));

        // 顶部按钮区域
        JPanel topBar=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        JButton btnRefreshAll=new JButton("刷新全部订单");
        JButton btnShowOpen=new JButton("只看未完成");
        JLabel filterLabel=new JLabel("按供应商筛选:");

        filterSupplierCombo=new JComboBox<>();
        JButton btnFilterBySupplier=new JButton("筛选");

        btnRefreshAll.addActionListener(e->loadAllOrders());
        btnShowOpen.addActionListener(e->loadOpenOrders());
        btnFilterBySupplier.addActionListener(e->onFilterBySupplier());

        topBar.add(btnRefreshAll);
        topBar.add(btnShowOpen);
        topBar.add(Box.createHorizontalStrut(20));
        topBar.add(filterLabel);
        topBar.add(filterSupplierCombo);
        topBar.add(btnFilterBySupplier);

        // 订单表格
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
        panel.setBorder(BorderFactory.createTitledBorder("订单列表"));
        return panel;
    }

    private JPanel createDetailPanel(){
        JPanel panel=new JPanel(new BorderLayout(5,5));
        orderDetailLabel=new JLabel("当前未选中订单");
        String[]cols={"明细ID","物资ID","数量","已收货","单价"};
        itemTableModel=new DefaultTableModel(cols,0){
            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        itemTable=new JTable(itemTableModel);
        JScrollPane scrollPane=new JScrollPane(itemTable);

        panel.add(orderDetailLabel,BorderLayout.NORTH);
        panel.add(scrollPane,BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createTitledBorder("订单详情"));
        return panel;
    }

    private JPanel createFormPanel(){
        JPanel panel=new JPanel(new GridLayout(1,2,10,10));

        // ===== 左边：创建订单 =====
        JPanel createOrderPanel=new JPanel(new GridBagLayout());
        createOrderPanel.setBorder(BorderFactory.createTitledBorder("创建新订单"));
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(5,5,5,5);
        gbc.anchor=GridBagConstraints.WEST;

        JLabel supplierLabel=new JLabel("供应商:");
        JLabel dateLabel=new JLabel("日期 (YYYY-MM-DD):");

        createOrderSupplierCombo=new JComboBox<>();
        createOrderDateField=new JTextField(10);
        // 默认填今天日期
        String today=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        createOrderDateField.setText(today);

        gbc.gridx=0;gbc.gridy=0;
        createOrderPanel.add(supplierLabel,gbc);
        gbc.gridx=1;
        createOrderPanel.add(createOrderSupplierCombo,gbc);

        gbc.gridx=0;gbc.gridy=1;
        createOrderPanel.add(dateLabel,gbc);
        gbc.gridx=1;
        createOrderPanel.add(createOrderDateField,gbc);

        JButton btnCreateOrder=new JButton("创建订单");
        btnCreateOrder.addActionListener(e->onCreateOrder());

        gbc.gridx=0;gbc.gridy=2;gbc.gridwidth=2;
        gbc.anchor=GridBagConstraints.CENTER;
        createOrderPanel.add(btnCreateOrder,gbc);

        // ===== 右边：为当前订单添加明细 =====
        JPanel addItemPanel=new JPanel(new GridBagLayout());
        addItemPanel.setBorder(BorderFactory.createTitledBorder("为当前选中订单添加明细"));
        GridBagConstraints gbc2=new GridBagConstraints();
        gbc2.insets=new Insets(5,5,5,5);
        gbc2.anchor=GridBagConstraints.WEST;

        JLabel materialLabel=new JLabel("物资:");
        JLabel quantityLabel=new JLabel("数量:");
        JLabel priceLabel=new JLabel("单价:");

        addItemMaterialCombo=new JComboBox<>();
        addItemQuantityField=new JTextField(8);
        addItemPriceField=new JTextField(8);

        gbc2.gridx=0;gbc2.gridy=0;
        addItemPanel.add(materialLabel,gbc2);
        gbc2.gridx=1;
        addItemPanel.add(addItemMaterialCombo,gbc2);

        gbc2.gridx=0;gbc2.gridy=1;
        addItemPanel.add(quantityLabel,gbc2);
        gbc2.gridx=1;
        addItemPanel.add(addItemQuantityField,gbc2);

        gbc2.gridx=0;gbc2.gridy=2;
        addItemPanel.add(priceLabel,gbc2);
        gbc2.gridx=1;
        addItemPanel.add(addItemPriceField,gbc2);

        JButton btnAddItem=new JButton("添加明细到当前订单");
        btnAddItem.addActionListener(e->onAddItem());

        gbc2.gridx=0;gbc2.gridy=3;gbc2.gridwidth=2;
        gbc2.anchor=GridBagConstraints.CENTER;
        addItemPanel.add(btnAddItem,gbc2);

        panel.add(createOrderPanel);
        panel.add(addItemPanel);

        return panel;
    }

    // ================== 内部工具 & 事件 ==================

    /**
     * 加载供应商列表，用于：
     * - 创建订单时选择供应商
     * - 订单筛选条件
     */
    private void loadSuppliersForCombos(){
        try{
            List<SupplierDto> suppliers=supplierService.listSuppliers();

            createOrderSupplierCombo.removeAllItems();
            filterSupplierCombo.removeAllItems();

            filterSupplierCombo.addItem(new ComboItem(-1,"(全部供应商)"));

            for(SupplierDto s:suppliers){
                ComboItem item=new ComboItem(s.id,s.name);
                createOrderSupplierCombo.addItem(item);
                filterSupplierCombo.addItem(new ComboItem(s.id,s.name));
            }
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "加载供应商列表失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 加载物资列表，用于添加明细时选择物资。
     */
    private void loadMaterialsForCombo(){
        try{
            List<MaterialDto> list=materialService.listMaterials();
            addItemMaterialCombo.removeAllItems();
            for(MaterialDto m:list){
                String label=m.name+" ("+m.spec+")";
                addItemMaterialCombo.addItem(new ComboItem(m.id,label));
            }
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "加载物资列表失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 加载全部订单。
     */
    private void loadAllOrders(){
        try{
            List<OrderSummaryDto> list=orderService.listOrders();
            fillOrderTable(list);
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "加载订单失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 加载未关闭订单。
     */
    private void loadOpenOrders(){
        try{
            List<OrderSummaryDto> list=orderService.listOpenOrders();
            fillOrderTable(list);
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

    private void onFilterBySupplier(){
        ComboItem item=(ComboItem)filterSupplierCombo.getSelectedItem();
        if(item==null || item.id==-1){
            loadAllOrders();
            return;
        }
        int supplierId=item.id;
        try{
            List<OrderSummaryDto> list=orderService.listOrdersBySupplier(supplierId);
            fillOrderTable(list);
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "按供应商筛选订单失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillOrderTable(List<OrderSummaryDto> list){
        orderTableModel.setRowCount(0);
        for(OrderSummaryDto o:list){
            Object[]row={o.id,o.supplierName,o.date,o.status};
            orderTableModel.addRow(row);
        }
        currentOrderId=-1;
        itemTableModel.setRowCount(0);
        orderDetailLabel.setText("当前未选中订单");
    }

    private void onOrderSelected(){
        int row=orderTable.getSelectedRow();
        if(row<0){
            currentOrderId=-1;
            itemTableModel.setRowCount(0);
            orderDetailLabel.setText("当前未选中订单");
            return;
        }

        int modelRow=orderTable.convertRowIndexToModel(row);
        int orderId=(int)orderTableModel.getValueAt(modelRow,0);
        loadOrderDetail(orderId);
    }

    private void loadOrderDetail(int orderId){
        try{
            OrderDetailDto detail=orderService.getOrderDetail(orderId);
            currentOrderId=detail.id;

            String label="订单ID="+detail.id
                         +" | 供应商="+detail.supplierName
                         +" | 日期="+detail.date
                         +" | 状态="+detail.status;
            orderDetailLabel.setText(label);

            itemTableModel.setRowCount(0);
            if(detail.items!=null){
                for(OrderItemDto it:detail.items){
                    Object[]row={it.id,it.materialId,it.quantity,it.receivedQuantity,it.price};
                    itemTableModel.addRow(row);
                }
            }
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

    private void onCreateOrder(){
        ComboItem supplierItem=(ComboItem)createOrderSupplierCombo.getSelectedItem();
        if(supplierItem==null){
            JOptionPane.showMessageDialog(this,"请先创建供应商并选择一个供应商",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }
        String date=createOrderDateField.getText().trim();
        if(date.isEmpty()){
            JOptionPane.showMessageDialog(this,"日期不能为空",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }

        try{
            int orderId=orderService.createOrder(supplierItem.id,date);
            JOptionPane.showMessageDialog(this,
                    "创建订单成功，ID="+orderId,
                    "成功",JOptionPane.INFORMATION_MESSAGE);

            // 刷新订单列表，并尝试选中新建的订单
            loadAllOrders();
            selectOrderInTable(orderId);
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "创建订单失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAddItem(){
        if(currentOrderId<=0){
            JOptionPane.showMessageDialog(this,"请先在上方选择一个订单",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }
        ComboItem materialItem=(ComboItem)addItemMaterialCombo.getSelectedItem();
        if(materialItem==null){
            JOptionPane.showMessageDialog(this,"请先创建物资并选择一个物资",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qtyText=addItemQuantityField.getText().trim();
        String priceText=addItemPriceField.getText().trim();
        int quantity;
        double price;
        try{
            quantity=Integer.parseInt(qtyText);
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this,"数量必须是整数",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }
        try{
            price=Double.parseDouble(priceText);
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this,"单价必须是数字",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }

        if(quantity<=0){
            JOptionPane.showMessageDialog(this,"数量必须大于 0",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(price<0){
            JOptionPane.showMessageDialog(this,"单价不能为负数",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }

        try{
            int itemId=orderService.addOrderItem(currentOrderId,materialItem.id,quantity,price);
            JOptionPane.showMessageDialog(this,
                    "添加明细成功，明细ID="+itemId,
                    "成功",JOptionPane.INFORMATION_MESSAGE);

            addItemQuantityField.setText("");
            addItemPriceField.setText("");

            // 刷新当前订单详情
            loadOrderDetail(currentOrderId);
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "添加明细失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectOrderInTable(int orderId){
        for(int i=0;i<orderTableModel.getRowCount();i++){
            int id=(int)orderTableModel.getValueAt(i,0);
            if(id==orderId){
                int viewRow=orderTable.convertRowIndexToView(i);
                orderTable.getSelectionModel().setSelectionInterval(viewRow,viewRow);
                break;
            }
        }
    }

    // 用于 JComboBox 的简单封装类
    private static class ComboItem {
        final int id;
        final String label;

        ComboItem(int id,String label){
            this.id=id;
            this.label=label;
        }

        @Override
        public String toString(){
            return label;
        }

    }
    @Override
    public void reloadData(){
        loadSuppliersForCombos();
        loadMaterialsForCombo();
        loadAllOrders();
    }
}
