package service;

import model.OrderDetailDto;
import model.OrderItemDto;
import model.OrderSummaryDto;
import org.json.JSONArray;
import org.json.JSONObject;
import protocol.ApiClient;
import protocol.ClientResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单前端服务。
 * 对应后端 API：
 * - CreateOrder
 * - AddOrderItem
 * - ListOrders
 * - GetOrderDetail
 * - ListOrdersBySupplier
 * - ListOpenOrders
 */
public class OrderClientService {
    private final ApiClient apiClient;
    private final AuthClientService auth;

    public OrderClientService(ApiClient apiClient,AuthClientService auth){
        this.apiClient=apiClient;
        this.auth=auth;
    }

    /**
     * 创建新订单。
     * @param supplierId 供应商 id
     * @param date       订单日期（例如 "2025-12-03"）
     * @return 新订单 id
     */
    public int createOrder(int supplierId,String date) throws IOException{
        JSONObject data=new JSONObject();
        data.put("supplierId",supplierId);
        data.put("date",date);

        ClientResponse resp=apiClient.call("CreateOrder",auth.getToken(),data);
        return resp.data.getInt("orderId");
    }

    /**
     * 为订单添加一条明细。
     * @return 新明细 id
     */
    public int addOrderItem(int orderId,int materialId,int quantity,double price) throws IOException{
        JSONObject data=new JSONObject();
        data.put("orderId",orderId);
        data.put("materialId",materialId);
        data.put("quantity",quantity);
        data.put("price",price);

        ClientResponse resp=apiClient.call("AddOrderItem",auth.getToken(),data);
        return resp.data.getInt("orderItemId");
    }

    /**
     * 查询所有订单概要列表。
     */
    public List<OrderSummaryDto> listOrders() throws IOException{
        JSONObject data=new JSONObject();
        ClientResponse resp=apiClient.call("ListOrders",auth.getToken(),data);
        return parseOrderSummaryList(resp.data.getJSONArray("orders"));
    }

    /**
     * 按供应商查询订单概要列表。
     */
    public List<OrderSummaryDto> listOrdersBySupplier(int supplierId) throws IOException{
        JSONObject data=new JSONObject();
        data.put("supplierId",supplierId);

        ClientResponse resp=apiClient.call("ListOrdersBySupplier",auth.getToken(),data);
        return parseOrderSummaryList(resp.data.getJSONArray("orders"));
    }

    /**
     * 查询所有未关闭（status!=CLOSED）的订单概要。
     */
    public List<OrderSummaryDto> listOpenOrders() throws IOException{
        JSONObject data=new JSONObject();
        ClientResponse resp=apiClient.call("ListOpenOrders",auth.getToken(),data);
        return parseOrderSummaryList(resp.data.getJSONArray("orders"));
    }

    /**
     * 获取单个订单的详细信息。
     */
    public OrderDetailDto getOrderDetail(int orderId) throws IOException{
        JSONObject data=new JSONObject();
        data.put("orderId",orderId);

        ClientResponse resp=apiClient.call("GetOrderDetail",auth.getToken(),data);
        JSONObject d=resp.data;

        OrderDetailDto dto=new OrderDetailDto();
        dto.id=d.getInt("id");
        dto.supplierId=d.getInt("supplierId");
        dto.supplierName=d.getString("supplierName");
        dto.date=d.getString("date");
        dto.status=d.getString("status");

        JSONArray itemsArr=d.getJSONArray("items");
        List<OrderItemDto> items=new ArrayList<>();
        for(int i=0;i<itemsArr.length();i++){
            JSONObject o=itemsArr.getJSONObject(i);
            OrderItemDto item=new OrderItemDto();
            item.id=o.getInt("id");
            item.orderId=o.getInt("orderId");
            item.materialId=o.getInt("materialId");
            item.quantity=o.getInt("quantity");
            item.price=o.getDouble("price");
            item.receivedQuantity=o.getInt("receivedQuantity");
            // materialName/spec 由后端返回则用之，否则为空字符串
            item.materialName=o.optString("materialName","");
            item.spec=o.optString("spec","");
            items.add(item);
        }
        dto.items=items;

        return dto;
    }

    // ===== 内部工具 =====

    private List<OrderSummaryDto> parseOrderSummaryList(JSONArray arr){
        List<OrderSummaryDto> list=new ArrayList<>();
        for(int i=0;i<arr.length();i++){
            JSONObject o=arr.getJSONObject(i);
            OrderSummaryDto dto=new OrderSummaryDto();
            dto.id=o.getInt("id");
            dto.supplierId=o.getInt("supplierId");
            dto.supplierName=o.optString("supplierName","");
            dto.date=o.getString("date");
            dto.status=o.getString("status");
            list.add(dto);
        }
        return list;
    }
}
