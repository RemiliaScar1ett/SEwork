package ui;

import model.MaterialDto;
import service.MaterialClientService;
import protocol.ApiException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * 物资管理面板：示范完整实现的 UI + service 调用。
 */
public class MaterialPanel extends JPanel {
    private final MaterialClientService materialService;

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField nameField;
    private JTextField specField;
    private JTextField unitField;

    public MaterialPanel(MaterialClientService materialService){
        this.materialService=materialService;
        initUi();
        loadMaterials();
    }

    private void initUi(){
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // 表格区
        String[]columns={"ID","名称","规格","单位"};
        tableModel=new DefaultTableModel(columns,0){
            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        table=new JTable(tableModel);
        JScrollPane scrollPane=new JScrollPane(table);

        // 表单区
        JPanel formPanel=new JPanel(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(5,5,5,5);
        gbc.anchor=GridBagConstraints.WEST;

        JLabel nameLabel=new JLabel("名称:");
        JLabel specLabel=new JLabel("规格:");
        JLabel unitLabel=new JLabel("单位:");

        nameField=new JTextField(15);
        specField=new JTextField(15);
        unitField=new JTextField(8);

        gbc.gridx=0;gbc.gridy=0;
        formPanel.add(nameLabel,gbc);
        gbc.gridx=1;
        formPanel.add(nameField,gbc);

        gbc.gridx=0;gbc.gridy=1;
        formPanel.add(specLabel,gbc);
        gbc.gridx=1;
        formPanel.add(specField,gbc);

        gbc.gridx=0;gbc.gridy=2;
        formPanel.add(unitLabel,gbc);
        gbc.gridx=1;
        formPanel.add(unitField,gbc);

        JButton addButton=new JButton("新增物资");
        addButton.addActionListener(e->onAddMaterial());

        gbc.gridx=0;gbc.gridy=3;gbc.gridwidth=2;
        gbc.anchor=GridBagConstraints.CENTER;
        formPanel.add(addButton,gbc);

        add(scrollPane,BorderLayout.CENTER);
        add(formPanel,BorderLayout.SOUTH);
    }

    private void loadMaterials(){
        SwingUtilities.invokeLater(()->{
            try{
                List<MaterialDto> list=materialService.listMaterials();
                tableModel.setRowCount(0);
                for(MaterialDto m:list){
                    Object[]row={m.id,m.name,m.spec,m.unit};
                    tableModel.addRow(row);
                }
            }catch(ApiException ex){
                JOptionPane.showMessageDialog(this,
                        "加载物资失败: "+ex.getMessage(),
                        "错误",JOptionPane.ERROR_MESSAGE);
            }catch(IOException ex){
                JOptionPane.showMessageDialog(this,
                        "网络错误: "+ex.getMessage(),
                        "错误",JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void onAddMaterial(){
        String name=nameField.getText().trim();
        String spec=specField.getText().trim();
        String unit=unitField.getText().trim();

        if(name.isEmpty()){
            JOptionPane.showMessageDialog(this,"名称不能为空",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(unit.isEmpty()){
            JOptionPane.showMessageDialog(this,"单位不能为空",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }

        try{
            int newId=materialService.createMaterial(name,spec,unit);
            JOptionPane.showMessageDialog(this,
                    "新增物资成功，ID="+newId,
                    "成功",JOptionPane.INFORMATION_MESSAGE);

            nameField.setText("");
            specField.setText("");
            unitField.setText("");

            loadMaterials();
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "新增物资失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }
}
