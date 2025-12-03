package service;

import model.SupplierDto;
import org.json.JSONArray;
import org.json.JSONObject;
import protocol.ApiClient;
import protocol.ClientResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 供应商管理前端服务。
 * 对应后端 API：
 * - CreateSupplier
 * - ListSuppliers
 */
public class SupplierClientService {
    private final ApiClient apiClient;
    private final AuthClientService auth;

    public SupplierClientService(ApiClient apiClient,AuthClientService auth){
        this.apiClient=apiClient;
        this.auth=auth;
    }

    /**
     * 创建供应商（仅管理员）。
     * @return 新供应商 id
     */
    public int createSupplier(String name,String contact,String phone) throws IOException{
        JSONObject data=new JSONObject();
        data.put("name",name);
        data.put("contact",contact);
        data.put("phone",phone);

        ClientResponse resp=apiClient.call("CreateSupplier",auth.getToken(),data);
        return resp.data.getInt("supplierId");
    }

    /**
     * 获取供应商列表。
     */
    public List<SupplierDto> listSuppliers() throws IOException{
        JSONObject data=new JSONObject();
        ClientResponse resp=apiClient.call("ListSuppliers",auth.getToken(),data);

        JSONArray arr=resp.data.getJSONArray("suppliers");
        List<SupplierDto> list=new ArrayList<>();
        for(int i=0;i<arr.length();i++){
            JSONObject o=arr.getJSONObject(i);
            SupplierDto dto=new SupplierDto();
            dto.id=o.getInt("id");
            dto.name=o.getString("name");
            dto.contact=o.getString("contact");
            dto.phone=o.getString("phone");
            list.add(dto);
        }
        return list;
    }
}
