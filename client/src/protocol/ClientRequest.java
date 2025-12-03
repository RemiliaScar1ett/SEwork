package protocol;

import org.json.JSONObject;

/**
 * 前端内部的请求对象，对应后端 JSON 的 requestId/action/auth/data。
 */
public class ClientRequest {
    public final String requestId;
    public final String action;
    public final String token;     // 可以为 null/空（如 Login）
    public final JSONObject data;  // 永不为 null

    public ClientRequest(String requestId,String action,String token,JSONObject data){
        this.requestId=requestId;
        this.action=action;
        this.token=token;
        this.data=data!=null?data:new JSONObject();
    }
}
