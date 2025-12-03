package service;

import model.StockDto;
import org.json.JSONArray;
import org.json.JSONObject;
import protocol.ApiClient;
import protocol.ClientResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 收货与库存相关前端服务。
 * 对应后端 API：
 * - ReceiveGoods
 * - GetStock
 * - ListStocks
 */
public class InventoryClientService {
    private final ApiClient apiClient;
    private final AuthClientService auth;

    public InventoryClientService(ApiClient apiClient,AuthClientService auth){
        this.apiClient=apiClient;
        this.auth=auth;
    }

    /**
     * 执行一次收货操作。
     * 若成功，后端会更新订单明细和库存；
     * 若参数非法（比如超收），会抛 ApiException(VALIDATION_ERROR)。
     */
    public void receiveGoods(int orderId,int orderItemId,int quantity) throws IOException{
        JSONObject data=new JSONObject();
        data.put("orderId",orderId);
        data.put("orderItemId",orderItemId);
        data.put("quantity",quantity);

        apiClient.call("ReceiveGoods",auth.getToken(),data);
        // 响应 data 中还有 orderClosed/receivedQuantity/currentStock，
        // 如需要可以在未来扩展为返回值。
    }

    /**
     * 获取某个物资的当前库存数量。
     */
    public int getStock(int materialId) throws IOException{
        JSONObject data=new JSONObject();
        data.put("materialId",materialId);

        ClientResponse resp=apiClient.call("GetStock",auth.getToken(),data);
        return resp.data.getInt("quantity");
    }

    /**
     * 获取所有物资的库存列表。
     */
    public List<StockDto> listStocks() throws IOException{
        JSONObject data=new JSONObject();
        ClientResponse resp=apiClient.call("ListStocks",auth.getToken(),data);

        JSONArray arr=resp.data.getJSONArray("stocks");
        List<StockDto> list=new ArrayList<>();
        for(int i=0;i<arr.length();i++){
            JSONObject o=arr.getJSONObject(i);
            StockDto dto=new StockDto();
            dto.materialId=o.getInt("materialId");
            dto.quantity=o.getInt("quantity");
            // materialName 可通过 MaterialClientService 缓存映射后填充
            dto.materialName=null;
            list.add(dto);
        }
        return list;
    }
}
