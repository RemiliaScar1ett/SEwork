package service;

import model.MaterialDto;
import org.json.JSONArray;
import org.json.JSONObject;
import protocol.ApiClient;
import protocol.ClientResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 物资管理前端服务。
 * 对应后端 API：
 * - CreateMaterial
 * - ListMaterials
 */
public class MaterialClientService {
    private final ApiClient apiClient;
    private final AuthClientService auth;

    public MaterialClientService(ApiClient apiClient,AuthClientService auth){
        this.apiClient=apiClient;
        this.auth=auth;
    }

    /**
     * 创建物资（仅管理员）。
     * @return 新物资 id
     */
    public int createMaterial(String name,String spec,String unit) throws IOException{
        JSONObject data=new JSONObject();
        data.put("name",name);
        data.put("spec",spec);
        data.put("unit",unit);

        ClientResponse resp=apiClient.call("CreateMaterial",auth.getToken(),data);
        return resp.data.getInt("materialId");
    }

    /**
     * 获取所有物资列表。
     */
    public List<MaterialDto> listMaterials() throws IOException{
        JSONObject data=new JSONObject();
        ClientResponse resp=apiClient.call("ListMaterials",auth.getToken(),data);

        JSONArray arr=resp.data.getJSONArray("materials");
        List<MaterialDto> list=new ArrayList<>();
        for(int i=0;i<arr.length();i++){
            JSONObject o=arr.getJSONObject(i);
            MaterialDto dto=new MaterialDto();
            dto.id=o.getInt("id");
            dto.name=o.getString("name");
            dto.spec=o.getString("spec");
            dto.unit=o.getString("unit");
            list.add(dto);
        }
        return list;
    }
}
