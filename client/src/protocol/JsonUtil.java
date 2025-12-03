package protocol;

import org.json.JSONObject;

/**
 * 请求/响应 与 JSON 文本之间的转换工具。
 */
public class JsonUtil {

    /**
     * 将 ClientRequest 转成后端约定的 JSON 字符串。
     */
    public static String toJsonString(ClientRequest req){
        JSONObject root=new JSONObject();

        if(req.requestId!=null && !req.requestId.isEmpty()){
            root.put("requestId",req.requestId);
        }
        root.put("action",req.action);

        if(req.token!=null && !req.token.isEmpty()){
            JSONObject auth=new JSONObject();
            auth.put("token",req.token);
            root.put("auth",auth);
        }

        root.put("data",req.data!=null?req.data:new JSONObject());

        return root.toString(); // 不包含换行符，TcpClient 会额外加 '\n'
    }

    /**
     * 将后端返回的 JSON 文本解析为 ClientResponse。
     */
    public static ClientResponse parseResponse(String jsonText){
        JSONObject root=new JSONObject(jsonText);

        String requestId=root.optString("requestId","");
        boolean success=root.getBoolean("success");
        String errorCode=root.optString("errorCode","");
        String message=root.optString("message","");

        JSONObject data=root.optJSONObject("data");
        if(data==null) data=new JSONObject();

        return new ClientResponse(requestId,success,errorCode,message,data);
    }
}
