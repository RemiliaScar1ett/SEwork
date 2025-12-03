package ui;

import model.UserDto;
import protocol.ApiException;
import service.UserClientService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * 用户管理面板：
 * - 展示用户列表
 * - 新建用户（用户名+密码+角色）
 * - 为选中用户重置密码
 *
 * 仅在管理员登录时由 MainFrame 添加到 Tab 中。
 */
public class UserPanel extends JPanel implements RefreshablePanel {
    private final UserClientService userService;

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;

    private JPasswordField resetPasswordField;

    public UserPanel(UserClientService userService){
        this.userService=userService;
        initUi();
        loadUsers();
    }

    private void initUi(){
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // ===== 表格区：用户列表 =====
        String[]columns={"ID","用户名","角色"};
        tableModel=new DefaultTableModel(columns,0){
            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        table=new JTable(tableModel);
        JScrollPane scrollPane=new JScrollPane(table);

        // ===== 下方操作区：分成两个 Group（新建 + 重置密码） =====
        JPanel bottomPanel=new JPanel(new GridLayout(1,2,10,10));

        // --- 新建用户区 ---
        JPanel createPanel=new JPanel(new GridBagLayout());
        createPanel.setBorder(BorderFactory.createTitledBorder("新建用户"));
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(5,5,5,5);
        gbc.anchor=GridBagConstraints.WEST;

        JLabel usernameLabel=new JLabel("用户名:");
        JLabel passwordLabel=new JLabel("密码:");
        JLabel roleLabel=new JLabel("角色:");

        usernameField=new JTextField(12);
        passwordField=new JPasswordField(12);
        roleCombo=new JComboBox<>(new String[]{"admin","user"});

        gbc.gridx=0;gbc.gridy=0;
        createPanel.add(usernameLabel,gbc);
        gbc.gridx=1;
        createPanel.add(usernameField,gbc);

        gbc.gridx=0;gbc.gridy=1;
        createPanel.add(passwordLabel,gbc);
        gbc.gridx=1;
        createPanel.add(passwordField,gbc);

        gbc.gridx=0;gbc.gridy=2;
        createPanel.add(roleLabel,gbc);
        gbc.gridx=1;
        createPanel.add(roleCombo,gbc);

        JButton createButton=new JButton("创建用户");
        createButton.addActionListener(e->onCreateUser());

        gbc.gridx=0;gbc.gridy=3;gbc.gridwidth=2;
        gbc.anchor=GridBagConstraints.CENTER;
        createPanel.add(createButton,gbc);

        // --- 重置密码区 ---
        JPanel resetPanel=new JPanel(new GridBagLayout());
        resetPanel.setBorder(BorderFactory.createTitledBorder("重置密码（选中上方用户）"));
        GridBagConstraints gbc2=new GridBagConstraints();
        gbc2.insets=new Insets(5,5,5,5);
        gbc2.anchor=GridBagConstraints.WEST;

        JLabel resetLabel=new JLabel("新密码:");

        resetPasswordField=new JPasswordField(12);

        gbc2.gridx=0;gbc2.gridy=0;
        resetPanel.add(resetLabel,gbc2);
        gbc2.gridx=1;
        resetPanel.add(resetPasswordField,gbc2);

        JButton resetButton=new JButton("重置密码");
        resetButton.addActionListener(e->onResetPassword());

        JButton refreshButton=new JButton("刷新列表");
        refreshButton.addActionListener(e->loadUsers());

        JPanel buttonsPanel=new JPanel(new FlowLayout(FlowLayout.CENTER,10,0));
        buttonsPanel.add(resetButton);
        buttonsPanel.add(refreshButton);

        gbc2.gridx=0;gbc2.gridy=1;gbc2.gridwidth=2;
        gbc2.anchor=GridBagConstraints.CENTER;
        resetPanel.add(buttonsPanel,gbc2);

        bottomPanel.add(createPanel);
        bottomPanel.add(resetPanel);

        add(scrollPane,BorderLayout.CENTER);
        add(bottomPanel,BorderLayout.SOUTH);
    }

    /**
     * 从后端加载用户列表并刷新表格。
     */
    private void loadUsers(){
        try{
            List<UserDto> users=userService.listUsers();
            tableModel.setRowCount(0);
            for(UserDto u:users){
                Object[]row={u.id,u.username,u.role};
                tableModel.addRow(row);
            }
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "加载用户列表失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * “创建用户”按钮的事件处理。
     */
    private void onCreateUser(){
        String username=usernameField.getText().trim();
        String password=new String(passwordField.getPassword());
        String role=(String)roleCombo.getSelectedItem();

        if(username.isEmpty()){
            JOptionPane.showMessageDialog(this,"用户名不能为空",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(password.isEmpty()){
            JOptionPane.showMessageDialog(this,"密码不能为空",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }

        try{
            int newId=userService.createUser(username,password,role);
            JOptionPane.showMessageDialog(this,
                    "创建用户成功，ID="+newId,
                    "成功",JOptionPane.INFORMATION_MESSAGE);

            usernameField.setText("");
            passwordField.setText("");

            loadUsers();
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "创建用户失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * “重置密码”按钮的事件处理。
     */
    private void onResetPassword(){
        int selectedRow=table.getSelectedRow();
        if(selectedRow<0){
            JOptionPane.showMessageDialog(this,
                    "请先在上方表格中选中一个用户",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newPassword=new String(resetPasswordField.getPassword());
        if(newPassword.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "新密码不能为空",
                    "提示",JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow=table.convertRowIndexToModel(selectedRow);
        int userId=(int)tableModel.getValueAt(modelRow,0);

        try{
            userService.resetPassword(userId,newPassword);
            JOptionPane.showMessageDialog(this,
                    "重置密码成功",
                    "成功",JOptionPane.INFORMATION_MESSAGE);
            resetPasswordField.setText("");
        }catch(ApiException ex){
            JOptionPane.showMessageDialog(this,
                    "重置密码失败: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,
                    "网络错误: "+ex.getMessage(),
                    "错误",JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void reloadData(){
        loadUsers();
    }
}
