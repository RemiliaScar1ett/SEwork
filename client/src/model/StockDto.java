package model;

/**
 * 用于库存展示的 DTO。
 * 对应 ListStocks / GetStock 返回的数据结构。
 */
public class StockDto {
    public int materialId;
    public String materialName;   // 可选：前端通过 MaterialDto 列表补充
    public int quantity;
}
