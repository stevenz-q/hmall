package com.hmall.common.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * MQ辅助类
 * 封装了常用的消息发送方法
 */
@Component
@ConditionalOnClass(RabbitTemplate.class)
@RequiredArgsConstructor
@Slf4j
public class RabbitMqHelper {

    private final RabbitTemplate rabbitTemplate;

    /**
     * @param exchange   交换机名
     * @param routingKey 路由值
     * @param msg        消息
     */
    public void sendMessage(String exchange, String routingKey, Object msg) {
        rabbitTemplate.convertAndSend(exchange, routingKey, msg);
    }

    /**
     * 发送延迟消息
     *
     * @param exchange   交换机名
     * @param routingKey 路由值
     * @param msg        消息
     * @param delay      延迟时间(毫秒)
     */
    public void sendDelayMessage(String exchange, String routingKey, Object msg, int delay) {
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, message -> {
            message.getMessageProperties().setDelay(delay);
            return message;
        });
    }

    /**
     * 发送生产者确认消息
     *
     * @param exchange   交换机名
     * @param routingKey 路由值
     * @param msg        消息
     * @param maxRetries 最大重试次数
     */
    public void sendMessageWithConfirm(String exchange, String routingKey, Object msg, int maxRetries) {
        CorrelationData correlationData = new CorrelationData();
        correlationData.getFuture().addCallback(new ListenableFutureCallback<CorrelationData.Confirm>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("SpringAMQP处理确认结果异常", ex);
            }

            @Override
            public void onSuccess(CorrelationData.Confirm result) {
                if (result.isAck()) {
                    log.debug("收到confirmCallback ack,消息发送成功!");
                } else {
                    log.error("收到confirmCallback nack,消息发送失败,reason{}", result.getReason());
                }
            }
        });
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, correlationData);
    }
}