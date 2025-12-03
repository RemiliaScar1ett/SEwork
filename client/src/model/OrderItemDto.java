package model;

/**
 * 订单中的一条明细记录。
 * 对应 GetOrderDetail 返回的 items 数组中的每一项。
 */
public class OrderItemDto {
    public int id;
    public int orderId;
    public int materialId;

    // 下面两个字段可能由后端直接返回，也可以前端通过 Material 列表补充
    public String materialName;
    public String spec;

    public int quantity;           // 订购数量
    public double price;           // 单价
    public int receivedQuantity;   // 已收货数量
}
