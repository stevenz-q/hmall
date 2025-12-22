package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.enumeration.OrderStatus;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单延迟消息监听类
 *
 * @author zhaoyq
 * @since 2025/9/13  16:32
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderDelayMessageListener {

    private final IOrderService orderService;
    private final PayClient payClient;

    /**
     * 检查订单支付状态,超时则返还库存数
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME, delayed = "true"),
            key = MQConstants.DELAY_ORDER_ROUTING_KEY
    ))
    public void ListenOrderMessageDelay(Long orderId) {
        Order order = orderService.getById(orderId);
        // 判断订单状态
        if (order == null || order.getStatus() != OrderStatus.UNPAID.getCode()) {
            // 订单已标记为已付款
            return;
        }
        // 查询支付订单
        PayOrderDTO payOrderDTO = payClient.queryPayOrderByBizOrderNo(orderId);
        if (payOrderDTO != null && payOrderDTO.getStatus() == 3) {
            // 支付订单为已付款:更新订单支付状态
            orderService.markOrderPaySuccess(orderId);
        } else {
            // 订单已过期:取消订单并返还库存
            orderService.cancelOrder(orderId);
        }
    }
}
