package service;

import model.AuthSession;
import org.json.JSONObject;
import protocol.ApiClient;
import protocol.ApiException;
import protocol.ClientResponse;

import java.io.IOException;

/**
 * 负责前端登录流程与会话信息维护。
 */
public class AuthClientService {
    private final ApiClient apiClient;
    private final AuthSession session=new AuthSession();

    // 可选：记录最近一次登录失败的信息，方便 UI 显示
    private String lastErrorMessage;

    public AuthClientService(ApiClient apiClient){
        this.apiClient=apiClient;
    }

    public AuthSession getSession(){
        return session;
    }

    /**
     * 使用用户名和密码进行登录。
     * @param username 用户名
     * @param password 密码
     * @return true 表示登录成功；false 表示失败（可能是网络错误或业务错误）
     */
    public boolean login(String username,String password){
        lastErrorMessage=null;

        JSONObject data=new JSONObject();
        data.put("username",username);
        data.put("password",password);

        try{
            ClientResponse resp=apiClient.call("Login",null,data);
            JSONObject d=resp.data;
            session.token=d.getString("token");
            session.userId=d.getInt("userId");
            session.username=d.getString("username");
            session.role=d.getString("role");
            return true;
        }catch(ApiException e){
            // 后端校验失败（INVALID_CREDENTIALS等）
            lastErrorMessage=e.getMessage();
            return false;
        }catch(IOException e){
            // 网络错误
            lastErrorMessage="网络错误: "+e.getMessage();
            return false;
        }
    }

    /**
     * 登出：仅清空本地会话，不通知后端（后端 token 简单长期有效）。
     */
    public void logout(){
        session.token=null;
        session.userId=0;
        session.username=null;
        session.role=null;
    }

    /**
     * 返回当前会话 token。若未登录则为 null。
     */
    public String getToken(){
        return session.token;
    }

    public boolean isAdmin(){
        return session.isAdmin();
    }

    public boolean isLoggedIn(){
        return session.isLoggedIn();
    }

    /**
     * 若最近一次 login() 失败，可通过此方法获取失败原因（可用于 UI 显示）。
     */
    public String getLastErrorMessage(){
        return lastErrorMessage;
    }
}
