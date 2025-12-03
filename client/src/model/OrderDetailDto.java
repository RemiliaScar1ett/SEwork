package model;

import java.util.List;

/**
 * 单个订单的详细信息，包括基本信息和明细项列表。
 * 对应 GetOrderDetail 返回的 data 对象。
 */
public class OrderDetailDto {
    public int id;
    public int supplierId;
    public String supplierName;
    public String date;
    public String status;          // "NEW" | "PARTIAL" | "CLOSED"

    public List<OrderItemDto> items;
}
