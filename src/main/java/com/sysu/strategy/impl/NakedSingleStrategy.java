package com.sysu.strategy.impl;

import com.sysu.pojo.Cell;
import com.sysu.pojo.Hint;
import com.sysu.pojo.Move;
import com.sysu.pojo.SudokuGrid;
import com.sysu.strategy.SudokuStrategy;

import java.util.*;

/**
 * Naked Single 策略
 * 当某个空格只有一个候选数时，该数字就是答案
 */
public class NakedSingleStrategy implements SudokuStrategy {

    @Override
    public String getStrategyName() {
        return "显性唯一 (Naked Single)";
    }

    @Override
    public int getPriority() {
        return 1; // 最高优先级
    }

    @Override
    public Hint findHint(SudokuGrid grid) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Cell cell = grid.getCell(row, col);
                
                // 如果格子为空且只有一个候选数
                if ((cell.getValue() == null || cell.getValue() == 0) && 
                    cell.getCandidates() != null && 
                    cell.getCandidates().size() == 1) {
                    
                    int value = cell.getCandidates().iterator().next();
                    
                    // 构造提示信息
                    List<Hint.CellPosition> primaryCells = Arrays.asList(
                        new Hint.CellPosition(row, col)
                    );
                    
                    Move suggestedMove = new Move(row, col, value, cell.getValue());
                    
                    String description = String.format(
                        "位置 (%d,%d) 只有一个可能的候选数 %d，因此这就是答案。",
                        row + 1, col + 1, value
                    );
                    
                    return new Hint(
                        getStrategyName(),
                        description,
                        primaryCells,
                        suggestedMove,
                        new HashMap<>() // Naked Single不需要消除候选数
                    );
                }
            }
        }
        return null;
    }

    @Override
    public boolean isApplicable(SudokuGrid grid) {
        // 只要有空格就可能适用
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Cell cell = grid.getCell(row, col);
                if (cell.getValue() == null || cell.getValue() == 0) {
                    return true;
                }
            }
        }
        return false;
    }
} 