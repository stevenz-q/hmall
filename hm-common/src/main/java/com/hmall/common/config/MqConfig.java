package com.hmall.common.config;

import com.hmall.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * @author zhaoyq
 * @since 2025/9/8  21:17
 */
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
@Slf4j
public class MqConfig {
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 添加消息发送前的处理器
        rabbitTemplate.addBeforePublishPostProcessors(message -> {
            MessageProperties properties = message.getMessageProperties();
            if (properties != null) {
                // 获取当前线程上下文中的用户信息
                Long userId = UserContext.getUser();
                if (userId != null) {
                    // 保存用户信息到请求头
                    log.debug("保存用户信息到RabbitMQ消息头: userId:{}", userId);
                    properties.setHeader("userInfo", userId);
                }
            }
            return message;
        });
        // 设置消息转换器
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
