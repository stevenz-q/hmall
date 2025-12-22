package com.hmall.trade.listener;

import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 支付状态监听器
 *
 * @author zhaoyq
 * @since 2025/9/8  21:31
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PayStatusListener {

    private final IOrderService orderService;

    /**
     * 支付状态成功:标记订单已支付
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.pay.success.queue", durable = "true"),
            exchange = @Exchange(name = "pay.direct"),
            key = "pay.success"
    ))
    public void listenPaySuccess(Long orderId) {
        log.info("接收到处理订单业务消息");
        Order order = orderService.getById(orderId);
        // 判断订单是否为未支付
        if (order == null || order.getStatus() != 1) {
            return;
        }
        orderService.markOrderPaySuccess(orderId);
    }
}
