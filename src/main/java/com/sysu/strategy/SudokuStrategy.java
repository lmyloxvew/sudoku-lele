package com.sysu.strategy;

import com.sysu.pojo.Hint;
import com.sysu.pojo.SudokuGrid;

/**
 * 数独解题策略接口
 * 所有具体的解题策略都需要实现这个接口
 */
public interface SudokuStrategy {
    
    /**
     * 策略名称
     * @return 策略的显示名称，如"Hidden Single"、"Naked Single"等
     */
    String getStrategyName();
    
    /**
     * 策略优先级
     * @return 数字越小优先级越高，用于决定提示时优先使用哪个策略
     */
    int getPriority();
    
    /**
     * 尝试应用这个策略找到下一步提示
     * @param grid 当前数独棋盘状态
     * @return 如果找到提示返回Hint对象，否则返回null
     */
    Hint findHint(SudokuGrid grid);
    
    /**
     * 检查这个策略是否适用于当前棋盘状态
     * @param grid 当前数独棋盘状态
     * @return 如果策略适用返回true
     */
    boolean isApplicable(SudokuGrid grid);
} 