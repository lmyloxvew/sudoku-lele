package com.sysu.strategy.impl;

import com.sysu.pojo.Cell;
import com.sysu.pojo.Hint;
import com.sysu.pojo.Move;
import com.sysu.pojo.SudokuGrid;
import com.sysu.strategy.SudokuStrategy;

import java.util.*;

/**
 * Hidden Single 策略
 * 当某个数字在行、列或九宫格中只有一个可能的位置时，就可以确定答案
 */
public class HiddenSingleStrategy implements SudokuStrategy {

    @Override
    public String getStrategyName() {
        return "隐性唯一 (Hidden Single)";
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public Hint findHint(SudokuGrid grid) {
        // 检查行中的隐性唯一
        Hint hint = checkRows(grid);
        if (hint != null) return hint;
        
        // 检查列中的隐性唯一
        hint = checkColumns(grid);
        if (hint != null) return hint;
        
        // 检查九宫格中的隐性唯一
        hint = checkBoxes(grid);
        if (hint != null) return hint;
        
        return null;
    }

    private Hint checkRows(SudokuGrid grid) {
        for (int row = 0; row < 9; row++) {
            for (int num = 1; num <= 9; num++) {
                List<Integer> possibleCols = new ArrayList<>();
                
                // 找到这个数字在当前行的所有可能位置
                for (int col = 0; col < 9; col++) {
                    Cell cell = grid.getCell(row, col);
                    if ((cell.getValue() == null || cell.getValue() == 0) &&
                        cell.getCandidates() != null &&
                        cell.getCandidates().contains(num)) {
                        possibleCols.add(col);
                    }
                }
                
                // 如果只有一个可能位置
                if (possibleCols.size() == 1) {
                    int col = possibleCols.get(0);
                    return createHint(grid, row, col, num, "行");
                }
            }
        }
        return null;
    }

    private Hint checkColumns(SudokuGrid grid) {
        for (int col = 0; col < 9; col++) {
            for (int num = 1; num <= 9; num++) {
                List<Integer> possibleRows = new ArrayList<>();
                
                // 找到这个数字在当前列的所有可能位置
                for (int row = 0; row < 9; row++) {
                    Cell cell = grid.getCell(row, col);
                    if ((cell.getValue() == null || cell.getValue() == 0) &&
                        cell.getCandidates() != null &&
                        cell.getCandidates().contains(num)) {
                        possibleRows.add(row);
                    }
                }
                
                // 如果只有一个可能位置
                if (possibleRows.size() == 1) {
                    int row = possibleRows.get(0);
                    return createHint(grid, row, col, num, "列");
                }
            }
        }
        return null;
    }

    private Hint checkBoxes(SudokuGrid grid) {
        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                for (int num = 1; num <= 9; num++) {
                    List<Hint.CellPosition> possiblePositions = new ArrayList<>();
                    
                    // 找到这个数字在当前九宫格的所有可能位置
                    for (int row = boxRow * 3; row < boxRow * 3 + 3; row++) {
                        for (int col = boxCol * 3; col < boxCol * 3 + 3; col++) {
                            Cell cell = grid.getCell(row, col);
                            if ((cell.getValue() == null || cell.getValue() == 0) &&
                                cell.getCandidates() != null &&
                                cell.getCandidates().contains(num)) {
                                possiblePositions.add(new Hint.CellPosition(row, col));
                            }
                        }
                    }
                    
                    // 如果只有一个可能位置
                    if (possiblePositions.size() == 1) {
                        Hint.CellPosition pos = possiblePositions.get(0);
                        return createHint(grid, pos.row(), pos.col(), num, "九宫格");
                    }
                }
            }
        }
        return null;
    }

    private Hint createHint(SudokuGrid grid, int row, int col, int num, String region) {
        Cell cell = grid.getCell(row, col);
        
        List<Hint.CellPosition> primaryCells = Arrays.asList(
            new Hint.CellPosition(row, col)
        );
        
        Move suggestedMove = new Move(row, col, num, cell.getValue());
        
        String description = String.format(
            "数字 %d 在第 %d %s中只能放在位置 (%d,%d)，因此这就是答案。",
            num, region.equals("行") ? row + 1 : (region.equals("列") ? col + 1 : ((row/3)*3 + (col/3) + 1)), 
            region, row + 1, col + 1
        );
        
        return new Hint(
            getStrategyName(),
            description,
            primaryCells,
            suggestedMove,
            new HashMap<>()
        );
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