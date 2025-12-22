package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * @author zhaoyq
 * @since 2025/8/14  01:48
 */
@FeignClient("order-service")
public interface OrderClient {
    /**
     * 标记订单已支付
     * @param orderId
     */
    @PutMapping("/{orderId}")
    void markOrderPaySuccess(@PathVariable("orderId") Long orderId);
}
