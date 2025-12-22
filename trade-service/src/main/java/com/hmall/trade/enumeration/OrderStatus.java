package com.hmall.trade.enumeration;

import lombok.Getter;

/**
 * 订单状态枚举
 *
 * @author zhaoyq
 * @since 2025/9/13  16:49
 */
@Getter
public enum OrderStatus {


    UNPAID(1, "未付款"),

    PAID(2, "已付款,未发货"),

    SHIPPED(3, "已发货,未确认"),

    COMPLETED(4, "确认收货，交易成功"),

    CANCELED(5, "交易取消，订单关闭"),

    REVIEWED(6, "交易结束，已评价");

    private final int code;
    private final String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的订单状态 code = " + code);

    }
}