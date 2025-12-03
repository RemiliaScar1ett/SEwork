package ui;

import net.TcpClient;
import service.*;
import model.AuthSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * 登录窗口：输入用户名/密码，登录成功后打开 MainFrame。
 */
public class LoginFrame extends JFrame {
    private final TcpClient tcpClient;
    private final AuthClientService authService;
    private final UserClientService userService;
    private final MaterialClientService materialService;
    private final SupplierClientService supplierService;
    private final OrderClientService orderService;
    private final InventoryClientService inventoryService;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame(TcpClient tcpClient,
                      AuthClientService authService,
                      UserClientService userService,
                      MaterialClientService materialService,
                      SupplierClientService supplierService,
                      OrderClientService orderService,
                      InventoryClientService inventoryService){
        this.tcpClient=tcpClient;
        this.authService=authService;
        this.userService=userService;
        this.materialService=materialService;
        this.supplierService=supplierService;
        this.orderService=orderService;
        this.inventoryService=inventoryService;

        initUi();
    }

    private void initUi(){
        setTitle("物资采购系统 - 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400,220);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel=new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JPanel formPanel=new JPanel(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(5,5,5,5);
        gbc.anchor=GridBagConstraints.WEST;

        JLabel userLabel=new JLabel("用户名:");
        JLabel passLabel=new JLabel("密码:");

        usernameField=new JTextField(20);
        passwordField=new JPasswordField(20);

        gbc.gridx=0;gbc.gridy=0;
        formPanel.add(userLabel,gbc);
        gbc.gridx=1;
        formPanel.add(usernameField,gbc);

        gbc.gridx=0;gbc.gridy=1;
        formPanel.add(passLabel,gbc);
        gbc.gridx=1;
        formPanel.add(passwordField,gbc);

        JButton loginButton=new JButton("登录");
        loginButton.addActionListener(this::onLoginClicked);

        // 回车触发登录
        passwordField.addActionListener(this::onLoginClicked);

        JPanel buttonPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(loginButton);

        statusLabel=new JLabel(" ");
        statusLabel.setForeground(Color.RED);

        panel.add(formPanel,BorderLayout.CENTER);
        panel.add(buttonPanel,BorderLayout.SOUTH);
        panel.add(statusLabel,BorderLayout.NORTH);

        setContentPane(panel);
    }

    private void onLoginClicked(ActionEvent e){
        String username=usernameField.getText().trim();
        String password=new String(passwordField.getPassword());

        if(username.isEmpty()||password.isEmpty()){
            statusLabel.setText("用户名和密码不能为空");
            return;
        }

        // 简单阻塞调用（作业级别可以接受）
        boolean ok=authService.login(username,password);
        if(!ok){
            String msg=authService.getLastErrorMessage();
            if(msg==null||msg.isEmpty()) msg="登录失败";
            statusLabel.setText(msg);
            return;
        }

        AuthSession session=authService.getSession();
        statusLabel.setText("登录成功: "+session.username+" ("+session.role+")，正在打开主界面...");

        SwingUtilities.invokeLater(()->{
            MainFrame mainFrame=new MainFrame(
                    tcpClient,
                    authService,
                    userService,
                    materialService,
                    supplierService,
                    orderService,
                    inventoryService
            );
            mainFrame.setVisible(true);
            dispose(); // 关闭登录窗口
        });
    }
}
