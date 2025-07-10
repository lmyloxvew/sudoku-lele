package com.sysu.strategy;

import com.sysu.pojo.Hint;
import com.sysu.pojo.SudokuGrid;
import com.sysu.strategy.impl.HiddenSingleStrategy;
import com.sysu.strategy.impl.NakedSingleStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 策略管理器
 * 负责管理所有解题策略并提供统一的提示接口
 */
@Component
public class StrategyManager {
    
    private final List<SudokuStrategy> strategies;
    
    public StrategyManager() {
        this.strategies = new ArrayList<>();
        initializeStrategies();
    }
    
    /**
     * 初始化所有可用的策略
     */
    private void initializeStrategies() {
        // 按照优先级添加策略
        strategies.add(new NakedSingleStrategy());
        strategies.add(new HiddenSingleStrategy());
        
        // 根据优先级排序
        strategies.sort(Comparator.comparingInt(SudokuStrategy::getPriority));
    }
    
    /**
     * 获取下一步提示
     * @param grid 当前棋盘状态
     * @return 找到的提示，如果没有找到返回null
     */
    public Hint getHint(SudokuGrid grid) {
        for (SudokuStrategy strategy : strategies) {
            if (strategy.isApplicable(grid)) {
                Hint hint = strategy.findHint(grid);
                if (hint != null) {
                    return hint;
                }
            }
        }
        return null;
    }
    
    /**
     * 添加新的策略
     * @param strategy 要添加的策略
     */
    public void addStrategy(SudokuStrategy strategy) {
        strategies.add(strategy);
        strategies.sort(Comparator.comparingInt(SudokuStrategy::getPriority));
    }
    
    /**
     * 获取所有策略
     * @return 策略列表
     */
    public List<SudokuStrategy> getAllStrategies() {
        return new ArrayList<>(strategies);
    }
} 