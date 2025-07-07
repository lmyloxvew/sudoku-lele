package com.sysu.pojo;

import java.util.HashSet;

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
    }

    // 获取一个单元格
    public Cell getCell(int row, int col) {
        return cells[row][col];
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