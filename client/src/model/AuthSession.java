package model;

/**
 * 保存当前登录会话信息。
 * 由 AuthClientService 负责填充，其他模块只读这些字段。
 */
public class AuthSession {
    public String token;     // 登录成功后返回的 token
    public int userId;       // 当前用户 id
    public String username;  // 当前用户名
    public String role;      // "admin" 或 "user"

    public boolean isAdmin(){
        return role!=null && role.equalsIgnoreCase("admin");
    }

    public boolean isLoggedIn(){
        return token!=null && !token.isEmpty();
    }
}
