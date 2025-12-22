package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author zhaoyq
 * @since 2025/8/14  01:42
 */
@FeignClient("user-service")
public interface UserClient {
    /**
     * 扣减余额
     * @param pw
     * @param amount
     */
    @PutMapping("/users/money/deduct")
    void deductMoney(@RequestParam("pw") String pw, @RequestParam("amount") Integer amount);
}
