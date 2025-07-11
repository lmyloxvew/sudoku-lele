package com.sysu.pojo;

import java.util.HashSet;
import java.util.Set;

/**
 * 代表9x9的数独棋盘。
 * 包含一个由81个Cell对象组成的二维数组。
 */
public class SudokuGrid implements Cloneable {

    // 81个单元格
    private Cell[][] cells;

    public SudokuGrid(String puzzleString) {
        this.cells = new Cell[9][9];
        // 根据81位的谜题字符串初始化所有单元格
        for (int i = 0; i < 81; i++) {
            int row = i / 9;
            int col = i % 9;
            char ch = puzzleString.charAt(i);
            int value = (ch == '.' || ch == '0') ? 0 : Character.getNumericValue(ch);
            cells[row][col] = new Cell(row, col, value, value != 0, new HashSet<>());
        }
        // 初始化后计算所有空格的候选数
        updateAllCandidates();
    }

    // 获取一个单元格
    public Cell getCell(int row, int col) {
        return cells[row][col];
    }
    
    // 获取所有单元格（用于策略访问）
    public Cell[][] getAllCells() {
        return cells;
    }

    /**
     * 验证指定位置填入某个数字是否合法
     * @param row 行号 (0-8)
     * @param col 列号 (0-8)
     * @param value 要填入的数字 (1-9)
     * @return 是否合法
     */
    public boolean isValidMove(int row, int col, int value) {
        // 输入验证
        if (row < 0 || row >= 9 || col < 0 || col >= 9) {
            throw new IndexOutOfBoundsException("行列坐标必须在0-8范围内，当前: (" + row + "," + col + ")");
        }
        
        if (value < 1 || value > 9) {
            return false;
        }
        
        // 检查行冲突
        for (int c = 0; c < 9; c++) {
            if (c != col && cells[row][c].getValue() != null && 
                cells[row][c].getValue() != 0 && cells[row][c].getValue().equals(value)) {
                return false;
            }
        }
        
        // 检查列冲突
        for (int r = 0; r < 9; r++) {
            if (r != row && cells[r][col].getValue() != null && 
                cells[r][col].getValue() != 0 && cells[r][col].getValue().equals(value)) {
                return false;
            }
        }
        
        // 检查九宫格冲突
        int boxStartRow = (row / 3) * 3;
        int boxStartCol = (col / 3) * 3;
        for (int r = boxStartRow; r < boxStartRow + 3; r++) {
            for (int c = boxStartCol; c < boxStartCol + 3; c++) {
                if ((r != row || c != col) && cells[r][c].getValue() != null && 
                    cells[r][c].getValue() != 0 && cells[r][c].getValue().equals(value)) {
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * 检查当前数独是否已完成
     * @return 如果所有格子都已填满且没有冲突则返回true
     */
    public boolean isCompleted() {
        // 检查是否所有格子都已填满
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (cells[row][col].getValue() == null || cells[row][col].getValue() == 0) {
                    return false;
                }
            }
        }
        
        // 检查是否有冲突
        return !hasConflicts();
    }

    /**
     * 检查当前棋盘是否有冲突
     * @return 有冲突返回true
     */
    public boolean hasConflicts() {
        // 检查所有行
        for (int row = 0; row < 9; row++) {
            Set<Integer> seen = new HashSet<>();
            for (int col = 0; col < 9; col++) {
                Integer value = cells[row][col].getValue();
                if (value != null && value != 0) {
                    if (seen.contains(value)) {
                        return true;
                    }
                    seen.add(value);
                }
            }
        }
        
        // 检查所有列
        for (int col = 0; col < 9; col++) {
            Set<Integer> seen = new HashSet<>();
            for (int row = 0; row < 9; row++) {
                Integer value = cells[row][col].getValue();
                if (value != null && value != 0) {
                    if (seen.contains(value)) {
                        return true;
                    }
                    seen.add(value);
                }
            }
        }
        
        // 检查所有九宫格
        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                Set<Integer> seen = new HashSet<>();
                for (int row = boxRow * 3; row < boxRow * 3 + 3; row++) {
                    for (int col = boxCol * 3; col < boxCol * 3 + 3; col++) {
                        Integer value = cells[row][col].getValue();
                        if (value != null && value != 0) {
                            if (seen.contains(value)) {
                                return true;
                            }
                            seen.add(value);
                        }
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * 计算指定位置的候选数
     * @param row 行号
     * @param col 列号
     * @return 可能的候选数集合
     */
    public Set<Integer> calculateCandidates(int row, int col) {
        Set<Integer> candidates = new HashSet<>();
        
        // 如果该位置已经有值，返回空集合
        if (cells[row][col].getValue() != null && cells[row][col].getValue() != 0) {
            return candidates;
        }
        
        // 从1-9开始，检查哪些数字可以填入
        for (int num = 1; num <= 9; num++) {
            if (isValidMove(row, col, num)) {
                candidates.add(num);
            }
        }
        
        return candidates;
    }

    /**
     * 更新所有空格的候选数
     */
    public void updateAllCandidates() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (cells[row][col].getValue() == null || cells[row][col].getValue() == 0) {
                    Set<Integer> candidates = calculateCandidates(row, col);
                    cells[row][col].setCandidates(candidates);
                }
            }
        }
    }

    /**
     * 获取某行已使用的数字
     */
    public Set<Integer> getUsedInRow(int row) {
        Set<Integer> used = new HashSet<>();
        for (int col = 0; col < 9; col++) {
            Integer value = cells[row][col].getValue();
            if (value != null && value != 0) {
                used.add(value);
            }
        }
        return used;
    }

    /**
     * 获取某列已使用的数字
     */
    public Set<Integer> getUsedInColumn(int col) {
        Set<Integer> used = new HashSet<>();
        for (int row = 0; row < 9; row++) {
            Integer value = cells[row][col].getValue();
            if (value != null && value != 0) {
                used.add(value);
            }
        }
        return used;
    }

    /**
     * 获取某九宫格已使用的数字
     */
    public Set<Integer> getUsedInBox(int row, int col) {
        Set<Integer> used = new HashSet<>();
        int boxStartRow = (row / 3) * 3;
        int boxStartCol = (col / 3) * 3;
        
        for (int r = boxStartRow; r < boxStartRow + 3; r++) {
            for (int c = boxStartCol; c < boxStartCol + 3; c++) {
                Integer value = cells[r][c].getValue();
                if (value != null && value != 0) {
                    used.add(value);
                }
            }
        }
        return used;
    }

    /**
     * 创建并返回此对象的一个深拷贝。
     * 这对于保存游戏状态快照至关重要。
     */
    @Override
    public SudokuGrid clone() {
        try {
            SudokuGrid newGrid = (SudokuGrid) super.clone();
            newGrid.cells = new Cell[9][9];
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    newGrid.cells[i][j] = this.cells[i][j].clone();
                }
            }
            return newGrid;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}