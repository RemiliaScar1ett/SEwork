package ui;

import model.SupplierDto;
import protocol.ApiException;
import service.SupplierClientService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * 供应商管理面板：
 * - 展示供应商列表
 * - 新建供应商（名称 + 联系人 + 电话）
 */
public class SupplierPanel extends JPanel implements RefreshablePanel {
    private final SupplierClientService supplierService;

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField nameField;
    private JTextField contactField;
    private JTextField phoneField;

    public SupplierPanel(SupplierClientService supplierService){
        this.supplierService=supplierService;
        initUi();
        loadSuppliers();
    }

    private void initUi(){
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // ===== 表格区：供应商列表 =====
        String[]columns={"ID","名称","联系人","电话"};
        tableModel=new DefaultTableModel(columns,0){
            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        table=new JTable(tableModel);
        JScrollPane scrollPane=new JScrollPane(table);

        // ===== 表单区：新增供应商 =====
        JPanel formPanel=new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("新增供应商"));
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(5,5,5,5);
        gbc.anchor=GridBagConstraints.WEST;

        JLabel nameLabel=new JLabel("名称:");
        JLabel contactLabel=new JLabel("联系人:");
        JLabel phoneLabel=new JLabel("电话:");

        nameField=new JTextField(15);
        contactField=new JTextField(12);
        phoneField=new JTextField(12);

        gbc.gridx=0;gbc.gridy=0;
        formPanel.add(nameLabel,gbc);
        gbc.gridx=1;
        formPanel.add(nameField,gbc);

        gbc.gridx=0;gbc.gridy=1;
        formPanel.add(contactLabel,gbc);
        gbc.gridx=1;
        formPanel.add(contactField,gbc);

        gbc.gridx=0;gbc.gridy=2;
        formPanel.add(phoneLabel,gbc);
        gbc.gridx=1;
        formPanel.add(phoneField,gbc);

        JButton addButton=new JButton("新增供应商");
        addButton.addActionListener(e->onAddSupplier());

        JButton refreshButton=new JButton("刷新列表");
        refreshButton.addActionListener(e->loadSuppliers());

        JPanel btnPanel=new JPanel(new FlowLayout(FlowLayout.CENTER,10,0));
        btnPanel.add(addButton);
        btnPanel.add(refreshButton);

        gbc.gridx=0;gbc.gridy=3;gbc.gridwidth=2;
        gbc.anchor=GridBagConstraints.CENTER;
        formPanel.add(btnPanel,gbc);

        add(scrollPane,BorderLayout.CENTER);
        add(formPanel,BorderLayout.SOUTH);
    }

    /**
     * 从后端加载供应商列表并刷新表格。
     */
    private void loadSuppliers(){
        try{
            List<SupplierDto> list=supplierService.listSuppliers();
            tableModel.setRowCount(0);
            for(SupplierDto s:list){
                Object[]row={s.id,s.name,s.contact,s.phone};
                tableModel.addRow(row);
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
     * “新增供应商”按钮事件。
     */
    private void onAddSupplier(){
        String name=nameField.getText().trim();
        String contact=contactField.getText().trim();
        String phone=phoneField.getText().trim();

        if(name.isEmpty()){
            JOptionPane.showMessageDialog(this,"名称不能为空",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }

        try{
            int newId=supplierService.createSupplier(name,contact,phone);
            JOptionPane.showMessageDialog(this,
                    "新增供应商成功，ID="+newId,
                    "成功",JOptionPane.INFORMATION_MESSAGE);

            nameField.setText("");
            contactField.setText("");
            phoneField.setText("");

            loadSuppliers();
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "新增供应商失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void reloadData(){
        loadSuppliers();
    }
}
