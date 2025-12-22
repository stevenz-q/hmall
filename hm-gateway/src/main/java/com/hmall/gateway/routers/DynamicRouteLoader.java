package com.hmall.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * 动态路由加载
 * 监听Nacos配置变更实现动态路由
 *
 * @author zhaoyq
 * @since 2025/8/19  16:31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final NacosConfigManager nacosConfigManager;
    private final RouteDefinitionWriter routeDefinitionWriter;

    private final String dataId = "gateway-router.json";
    private final String group = "DEFAULT_GROUP";

    private final Set<String> routerIds = new HashSet<>();

    @PostConstruct
    public void initRouterConfigListener() throws NacosException {
        // 拉取配置并添加配置监听器
        String configInfo = nacosConfigManager.getConfigService().getConfigAndSignListener(
                dataId, group, 5, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        // 获取到配置变更,更新路由表
                        updateConfigInfo(configInfo);
                    }
                }
        );
        // 更新到路由表
        updateConfigInfo(configInfo);
    }

    public void updateConfigInfo(String configInfo) {
        log.debug("监听到路由配置信息:{}", configInfo);
        // 解析配置信息,转为RouteDefinition
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        // 删除旧的路由表
        for (String routerId : routerIds) {
            routeDefinitionWriter.delete(Mono.just(routerId)).subscribe();

        }
        routerIds.clear();
        // 更新路由表
        for (RouteDefinition routeDefinition : routeDefinitions) {
            // 订阅式更新
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            routerIds.add(routeDefinition.getId());
        }
    }
}
