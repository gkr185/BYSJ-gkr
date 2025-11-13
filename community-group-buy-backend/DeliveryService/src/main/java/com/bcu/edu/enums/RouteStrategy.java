package com.bcu.edu.enums;

/**
 * 路径规划策略枚举
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
public enum RouteStrategy {
    
    /**
     * 最短时间优先
     */
    SHORTEST_TIME(0, "shortest-time", "最短时间"),
    
    /**
     * 最短距离优先
     */
    SHORTEST_DISTANCE(1, "shortest-distance", "最短距离"),
    
    /**
     * 避开拥堵
     */
    AVOID_CONGESTION(2, "avoid-congestion", "避开拥堵");
    
    private final Integer code;
    private final String strategy;
    private final String description;
    
    RouteStrategy(Integer code, String strategy, String description) {
        this.code = code;
        this.strategy = strategy;
        this.description = description;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getStrategy() {
        return strategy;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据策略字符串获取枚举
     */
    public static RouteStrategy fromStrategy(String strategy) {
        if (strategy == null) {
            return SHORTEST_TIME; // 默认策略
        }
        for (RouteStrategy routeStrategy : RouteStrategy.values()) {
            if (routeStrategy.getStrategy().equals(strategy)) {
                return routeStrategy;
            }
        }
        return SHORTEST_TIME; // 默认策略
    }
}
