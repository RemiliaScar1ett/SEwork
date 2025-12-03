package protocol;

import net.TcpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 高层协议客户端：
 * - 负责封装 requestId、action、token、data
 * - 调用 TcpClient 发送/接收
 * - 解析响应并在失败时抛出 ApiException
 */
public class ApiClient {
    private final TcpClient tcpClient;
    private final AtomicInteger requestCounter=new AtomicInteger(1);

    public ApiClient(TcpClient tcpClient){
        this.tcpClient=tcpClient;
    }

    /**
     * 调用一个后端 API。
     * @param action 后端 action 名（如 "Login"、"CreateOrder"）
     * @param token  登录后获得的 token；对于 Login 等不需要 token 的请求传 null 或 ""
     * @param data   请求 data 对象；可为空，内部会转成 {}
     * @return       成功时返回解析后的 ClientResponse（success 一定为 true）
     * @throws IOException 网络错误时抛出
     * @throws ApiException 后端返回 success=false 时抛出
     */
    public ClientResponse call(String action,String token,JSONObject data) throws IOException{
        String requestId=String.valueOf(requestCounter.getAndIncrement());
        ClientRequest req=new ClientRequest(requestId,action,token,data);

        String json=JsonUtil.toJsonString(req);
        String respLine=tcpClient.sendAndReceive(json);

        ClientResponse resp=JsonUtil.parseResponse(respLine);

        if(!resp.success){
            throw new ApiException(resp.errorCode,resp.message);
        }
        return resp;
    }

    /**
     * 便捷封装：无 data 的请求。
     */
    public ClientResponse call(String action,String token) throws IOException{
        return call(action,token,new JSONObject());
    }
}
