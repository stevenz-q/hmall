package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 购物车条目管理监听器
 *
 * @author zhaoyq
 * @since 2025/9/8  21:31
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteCartItemListener {

    private final ICartService cartService;

    /**
     * 清理购物车
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "cart.clear.queue", durable = "true"),
            exchange = @Exchange(name = "trade.topic"),
            key = "order.create"
    ))
    public void listenDeleteCartItem(@Headers Map<String, Object> headers, @Payload Set<Long> itemIds) {
        Long userId = (Long) headers.get("userInfo");
        log.info("接收到清理购物车消息,userId:{},itemIds:{}", userId, itemIds);
        UserContext.setUser(userId);
        cartService.removeByItemIds(itemIds);
        UserContext.removeUser();
    }
}