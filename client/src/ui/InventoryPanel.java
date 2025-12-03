package ui;

import model.MaterialDto;
import model.StockDto;
import protocol.ApiException;
import service.InventoryClientService;
import service.MaterialClientService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存查询面板：
 * - 显示所有物资当前库存列表
 * - 支持选择某个物资单独查询库存
 */
public class InventoryPanel extends JPanel implements RefreshablePanel {
    private final InventoryClientService inventoryService;
    private final MaterialClientService materialService;

    private JTable stockTable;
    private DefaultTableModel stockTableModel;

    private JComboBox<ComboItem> materialCombo;
    private JLabel singleStockLabel;

    // materialId -> MaterialDto 映射，用于显示名称
    private Map<Integer,MaterialDto> materialMap=new HashMap<>();

    public InventoryPanel(InventoryClientService inventoryService,
                          MaterialClientService materialService){
        this.inventoryService=inventoryService;
        this.materialService=materialService;
        initUi();
        loadMaterials();
        loadAllStocks();
    }

    private void initUi(){
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // ===== 中间：库存总览表格 =====
        JPanel tablePanel=new JPanel(new BorderLayout(5,5));
        tablePanel.setBorder(BorderFactory.createTitledBorder("库存总览"));

        String[]columns={"物资ID","物资名称","规格","单位","数量"};
        stockTableModel=new DefaultTableModel(columns,0){
            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        stockTable=new JTable(stockTableModel);
        JScrollPane scrollPane=new JScrollPane(stockTable);

        JButton refreshButton=new JButton("刷新库存列表");
        refreshButton.addActionListener(e->loadAllStocks());

        JPanel topBar=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        topBar.add(refreshButton);

        tablePanel.add(topBar,BorderLayout.NORTH);
        tablePanel.add(scrollPane,BorderLayout.CENTER);

        // ===== 下方：单个物资查询 =====
        JPanel singlePanel=new JPanel(new GridBagLayout());
        singlePanel.setBorder(BorderFactory.createTitledBorder("单个物资库存查询"));

        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(5,5,5,5);
        gbc.anchor=GridBagConstraints.WEST;

        JLabel materialLabel=new JLabel("物资:");
        materialCombo=new JComboBox<>();

        JButton queryButton=new JButton("查询库存");
        queryButton.addActionListener(e->onQuerySingleStock());

        singleStockLabel=new JLabel("当前库存: -");

        gbc.gridx=0;gbc.gridy=0;
        singlePanel.add(materialLabel,gbc);
        gbc.gridx=1;
        singlePanel.add(materialCombo,gbc);

        gbc.gridx=0;gbc.gridy=1;gbc.gridwidth=2;
        gbc.anchor=GridBagConstraints.CENTER;
        singlePanel.add(queryButton,gbc);

        gbc.gridx=0;gbc.gridy=2;gbc.gridwidth=2;
        singlePanel.add(singleStockLabel,gbc);

        add(tablePanel,BorderLayout.CENTER);
        add(singlePanel,BorderLayout.SOUTH);
    }

    /**
     * 从后端加载物资列表，构造 materialId -> MaterialDto 映射，
     * 供库存表格显示，以及下拉框选择。
     */
    private void loadMaterials(){
        try{
            List<MaterialDto> list=materialService.listMaterials();
            materialMap.clear();
            materialCombo.removeAllItems();

            for(MaterialDto m:list){
                materialMap.put(m.id,m);
                String label=m.name+" ("+m.spec+")";
                materialCombo.addItem(new ComboItem(m.id,label));
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
     * 加载所有物资的库存信息。
     */
    private void loadAllStocks(){
        try{
            List<StockDto> stocks=inventoryService.listStocks();
            stockTableModel.setRowCount(0);

            for(StockDto s:stocks){
                MaterialDto m=materialMap.get(s.materialId);
                String name=m!=null?m.name:"(未知物资)";
                String spec=m!=null?m.spec:"";
                String unit=m!=null?m.unit:"";

                Object[]row={s.materialId,name,spec,unit,s.quantity};
                stockTableModel.addRow(row);
            }
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "加载库存列表失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * “查询库存”按钮：根据下拉框选中的物资，调用 GetStock。
     */
    private void onQuerySingleStock(){
        ComboItem item=(ComboItem)materialCombo.getSelectedItem();
        if(item==null){
            JOptionPane.showMessageDialog(this,
                    "请先在上方创建物资，并选择一个物资",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }
        int materialId=item.id;
        try{
            int qty=inventoryService.getStock(materialId);
            MaterialDto m=materialMap.get(materialId);
            String name=m!=null?m.name:"(未知物资)";
            singleStockLabel.setText("物资 ["+materialId+"] "+name+" 当前库存: "+qty);
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "查询库存失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    // JComboBox 用的小封装
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
        loadAllStocks();
        loadMaterials();
    }
}
