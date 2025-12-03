package service;

import model.UserDto;
import org.json.JSONArray;
import org.json.JSONObject;
import protocol.ApiClient;
import protocol.ClientResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 前端的用户管理服务。
 * 对应后端的用户管理 API：
 * - CreateUser
 * - ListUsers
 * - ResetPassword
 */
public class UserClientService {
    private final ApiClient apiClient;
    private final AuthClientService auth;

    public UserClientService(ApiClient apiClient,AuthClientService auth){
        this.apiClient=apiClient;
        this.auth=auth;
    }

    /**
     * 创建用户（仅管理员）。
     * @return 新用户的 id
     * @throws IOException 网络错误
     * @throws protocol.ApiException 后端业务错误（非 admin、用户名重复等）
     */
    public int createUser(String username,String password,String role) throws IOException{
        JSONObject data=new JSONObject();
        data.put("username",username);
        data.put("password",password);
        data.put("role",role); // "admin" 或 "user"

        ClientResponse resp=apiClient.call("CreateUser",auth.getToken(),data);
        return resp.data.getInt("userId");
    }

    /**
     * 获取所有用户列表（仅管理员）。
     */
    public List<UserDto> listUsers() throws IOException{
        JSONObject data=new JSONObject();
        ClientResponse resp=apiClient.call("ListUsers",auth.getToken(),data);

        JSONArray arr=resp.data.getJSONArray("users");
        List<UserDto> result=new ArrayList<>();
        for(int i=0;i<arr.length();i++){
            JSONObject o=arr.getJSONObject(i);
            UserDto dto=new UserDto();
            dto.id=o.getInt("id");
            dto.username=o.getString("username");
            dto.role=o.getString("role");
            result.add(dto);
        }
        return result;
    }

    /**
     * 重置指定用户密码（仅管理员）。
     */
    public void resetPassword(int userId,String newPassword) throws IOException{
        JSONObject data=new JSONObject();
        data.put("userId",userId);
        data.put("newPassword",newPassword);
        apiClient.call("ResetPassword",auth.getToken(),data);
        // 若失败会抛 ApiException；成功 data 为 {}
    }
}
