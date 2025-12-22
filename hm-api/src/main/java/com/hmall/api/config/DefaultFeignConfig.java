package com.hmall.api.config;

import com.hmall.api.client.fallback.ItemClientFallbackFactory;
import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * Feign配置类
 *
 * @author zhaoyq
 * @since 2025/8/13  23:02
 */
public class DefaultFeignConfig {
    /**
     * 配置Feign的日志等级
     *
     * @return
     */
    @Bean
    public Logger.Level feignLogLevel() {
        return Logger.Level.BASIC;
        //return Logger.Level.FULL;
    }

    /**
     * 配置Feign拦截器,添加用户信息到header
     *
     * @return
     */
    @Bean
    public RequestInterceptor userInfoRequestInterceptor() {
        return template -> {
            Long userId = UserContext.getUser();
            if (userId != null) {
                template.header("user-info", userId.toString());
            }
        };
    }

    /**
     * 配置Fallback降级逻辑工厂
     *
     * @return
     */
    @Bean
    public ItemClientFallbackFactory itemClientFallbackFactory() {
        return new ItemClientFallbackFactory();
    }

}
