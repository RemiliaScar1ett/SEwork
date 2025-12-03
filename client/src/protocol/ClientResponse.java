package protocol;

import org.json.JSONObject;

/**
 * 前端内部的响应对象，对应后端 JSON 的 success/errorCode/message/data。
 */
public class ClientResponse {
    public final String requestId;
    public final boolean success;
    public final String errorCode;
    public final String message;
    public final JSONObject data; // 永不为 null

    public ClientResponse(String requestId,boolean success,String errorCode,String message,JSONObject data){
        this.requestId=requestId;
        this.success=success;
        this.errorCode=errorCode!=null?errorCode:"";
        this.message=message!=null?message:"";
        this.data=data!=null?data:new JSONObject();
    }
}
