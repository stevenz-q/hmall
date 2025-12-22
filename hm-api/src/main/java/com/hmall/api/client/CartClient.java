package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

/**
 * @author zhaoyq
 * @since 2025/8/14  00:37
 */
@FeignClient("cart-service")
public interface CartClient {
    /**
     * 批量删除购物车中商品
     * @param ids 购物车条目id集合
     */
    @DeleteMapping("/carts")
    void deleteCartItemByIds(@RequestParam("ids") Collection<Long> ids);
}
