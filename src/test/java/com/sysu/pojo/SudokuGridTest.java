package com.sysu.pojo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

/**
 * SudokuGrid单元测试
 */
public class SudokuGridTest {

    private SudokuGrid grid;
    private String testPuzzle;
    private String completedPuzzle;
    private String invalidPuzzle;

    @BeforeEach
    void setUp() {
        // 测试用的数独谜题（有解）
        testPuzzle = "003020600900305001001806400008102900700000008006708200002609500800203009005010300";
        
        // 已完成的数独
        completedPuzzle = "483921657967345821251876493548132976729564138136798245372689514814253769695417382";
        
        // 无效的数独（行冲突）
        invalidPuzzle = "113020600900305001001806400008102900700000008006708200002609500800203009005010300";
        
        grid = new SudokuGrid(testPuzzle);
    }

    @Test
    void testGridInitialization() {
        // 测试网格初始化
        assertNotNull(grid);
        
        // 检查第一个单元格
        Cell cell = grid.getCell(0, 0);
        assertEquals(0, cell.getValue()); // 空格应该为0
        assertFalse(cell.isGiven());
        
        // 检查给定数字
        cell = grid.getCell(0, 2);
        assertEquals(3, cell.getValue());
        assertTrue(cell.isGiven());
    }

    @Test
    void testIsValidMove() {
        // 添加调试信息
        System.out.println("测试数据: " + testPuzzle);
        System.out.println("第0行已使用数字: " + grid.getUsedInRow(0));
        System.out.println("第0列已使用数字: " + grid.getUsedInColumn(0));
        System.out.println("左上九宫格已使用数字: " + grid.getUsedInBox(0, 0));
        
        // 检查(0,0)位置的当前值
        System.out.println("(0,0)当前值: " + grid.getCell(0, 0).getValue());
        
        // 连续调用检查状态变化
        System.out.println("第1次调用 isValidMove(0,0,4) = " + grid.isValidMove(0, 0, 4));
        System.out.println("第2次调用 isValidMove(0,0,4) = " + grid.isValidMove(0, 0, 4));
        System.out.println("第3次调用 isValidMove(0,0,4) = " + grid.isValidMove(0, 0, 4));
        
        // 测试有效移动
        boolean result = grid.isValidMove(0, 0, 4);
        System.out.println("断言前 isValidMove(0,0,4) = " + result);
        assertTrue(result); // 在空格位置填入4应该有效
        assertTrue(grid.isValidMove(0, 0, 5)); // 在空格位置填入5应该有效（第0列没有5）
        
        // 测试无效移动（行冲突）
        assertFalse(grid.isValidMove(0, 0, 3)); // 第0行已经有3了
        
        // 测试无效移动（列冲突）
        assertFalse(grid.isValidMove(0, 0, 9)); // 第0列已经有9了
        assertFalse(grid.isValidMove(0, 0, 8)); // 第0列已经有8了
        
        // 测试无效移动（九宫格冲突）
        assertFalse(grid.isValidMove(1, 1, 9)); // 左上九宫格已经有9了
        
        // 测试无效数字
        assertFalse(grid.isValidMove(0, 0, 0));
        assertFalse(grid.isValidMove(0, 0, 10));
        
        // 最后检查候选数（可能有副作用）
        System.out.println("(0,0)位置候选数: " + grid.calculateCandidates(0, 0));
    }

    @Test
    void testCandidateCalculation() {
        // 测试候选数计算
        Set<Integer> candidates = grid.calculateCandidates(0, 0);
        
        // 验证候选数不包含已存在的数字
        assertFalse(candidates.contains(3)); // 行冲突
        assertFalse(candidates.contains(9)); // 列冲突
        
        // 验证候选数在有效范围内
        for (int candidate : candidates) {
            assertTrue(candidate >= 1 && candidate <= 9);
            assertTrue(grid.isValidMove(0, 0, candidate));
        }
    }

    @Test
    void testHasConflicts() {
        // 正常谜题不应该有冲突
        assertFalse(grid.hasConflicts());
        
        // 创建有冲突的网格
        SudokuGrid conflictGrid = new SudokuGrid(invalidPuzzle);
        assertTrue(conflictGrid.hasConflicts());
    }

    @Test
    void testIsCompleted() {
        // 未完成的谜题
        assertFalse(grid.isCompleted());
        
        // 已完成的谜题
        SudokuGrid completedGrid = new SudokuGrid(completedPuzzle);
        assertTrue(completedGrid.isCompleted());
    }

    @Test
    void testGetUsedInRow() {
        Set<Integer> usedInRow0 = grid.getUsedInRow(0);
        
        // 第0行应该包含3, 2, 6
        assertTrue(usedInRow0.contains(3));
        assertTrue(usedInRow0.contains(2));
        assertTrue(usedInRow0.contains(6));
        
        // 不应该包含其他数字
        assertFalse(usedInRow0.contains(1));
        assertFalse(usedInRow0.contains(4));
    }

    @Test
    void testGetUsedInColumn() {
        Set<Integer> usedInCol0 = grid.getUsedInColumn(0);
        
        // 第0列应该包含9, 7, 8
        assertTrue(usedInCol0.contains(9));
        assertTrue(usedInCol0.contains(7));
        assertTrue(usedInCol0.contains(8));
        
        // 不应该包含其他数字（如5）
        assertFalse(usedInCol0.contains(5));
    }

    @Test
    void testGetUsedInBox() {
        // 测试左上九宫格 (0,0)
        Set<Integer> usedInBox = grid.getUsedInBox(0, 0);
        
        // 左上九宫格应该包含3, 9, 1
        assertTrue(usedInBox.contains(3));
        assertTrue(usedInBox.contains(9));
        assertTrue(usedInBox.contains(1));
    }

    @Test
    void testUpdateAllCandidates() {
        // 手动修改一个单元格的值
        Cell cell = grid.getCell(0, 0);
        cell.setValue(4);
        
        // 更新所有候选数
        grid.updateAllCandidates();
        
        // 检查相关位置的候选数是否正确更新
        Set<Integer> candidates = grid.getCell(0, 1).getCandidates();
        assertFalse(candidates.contains(4)); // 不应该包含4（行冲突）
    }

    @Test
    void testGridClone() {
        // 测试深拷贝
        SudokuGrid clonedGrid = grid.clone();
        
        assertNotSame(grid, clonedGrid);
        assertNotSame(grid.getAllCells(), clonedGrid.getAllCells());
        
        // 修改原网格不应该影响克隆网格
        grid.getCell(0, 0).setValue(5);
        assertNotEquals(grid.getCell(0, 0).getValue(), clonedGrid.getCell(0, 0).getValue());
    }
} 