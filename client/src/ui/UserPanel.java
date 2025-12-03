package ui;

import service.UserClientService;

import javax.swing.*;
import java.awt.*;

/**
 * 用户管理面板（占位版本，后续可补表格和按钮）。
 */
public class UserPanel extends JPanel {
    private final UserClientService userService;

    public UserPanel(UserClientService userService){
        this.userService=userService;
        initUi();
    }

    private void initUi(){
        setLayout(new BorderLayout());
        add(new JLabel("用户管理功能（待实现）",SwingConstants.CENTER),BorderLayout.CENTER);
    }
}
