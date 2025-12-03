package model;

/**
 * 用于订单列表展示的简要信息。
 * 对应 ListOrders / ListOrdersBySupplier / ListOpenOrders 返回的每一项。
 */
public class OrderSummaryDto {
    public int id;
    public int supplierId;
    public String supplierName;
    public String date;     // 例如 "2025-12-03"
    public String status;   // "NEW" | "PARTIAL" | "CLOSED"
}
