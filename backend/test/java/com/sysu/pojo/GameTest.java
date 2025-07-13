package com.sysu.pojo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Stack;

/**
 * Game类的单元测试
 */
public class GameTest {

    private Game game;
    private String testPuzzle;

    @BeforeEach
    void setUp() {
        // 使用一个简单的测试谜题
        testPuzzle = "003020600900305001001806400008102900700000008006708200002609500800203009005010300";

        SudokuGrid grid = new SudokuGrid(testPuzzle);
        game = new Game(1, grid, GameStatus.IN_PROGRESS, new Stack<>(), new Stack<>(), LocalDateTime.now(), null);
    }

    @Test
    void testInitialGridSaved() {
        // 验证初始棋盘状态被正确保存
        assertNotNull(game.getInitialGrid());
        assertNotSame(game.getGrid(), game.getInitialGrid()); // 应该是不同的对象

        // 验证初始状态相同
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                assertEquals(
                        game.getInitialGrid().getCell(row, col).getValue(),
                        game.getGrid().getCell(row, col).getValue(),
                        "位置(" + row + "," + col + ")的值应该相同"
                );
            }
        }
    }

    @Test
    void testMakeMoveAndReset() {
        // 记录初始状态
        Integer initialValue = game.getGrid().getCell(0, 0).getValue();

        // 进行一步移动
        Move move = new Move(0, 0, 4, null);
        boolean moveResult = game.makeMove(move);
        assertTrue(moveResult, "移动应该成功");
        assertEquals(4, game.getGrid().getCell(0, 0).getValue(), "移动后值应该是4");
        assertEquals(1, game.getMoveHistory().size(), "移动历史应该有1条记录");

        // 重置游戏
        game.resetToInitialState();

        // 验证重置结果
        assertEquals(initialValue, game.getGrid().getCell(0, 0).getValue(), "重置后应该恢复初始值");
        assertEquals(0, game.getMoveHistory().size(), "移动历史应该被清空");
        assertEquals(0, game.getRedoHistory().size(), "重做历史应该被清空");
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus(), "游戏状态应该是进行中");
        assertNull(game.getEndTime(), "结束时间应该被清空");
    }

    @Test
    void testMultipleMovesAndReset() {
        // 进行多步移动
        Move move1 = new Move(0, 0, 4, null);
        Move move2 = new Move(0, 1, 5, null);

        game.makeMove(move1);
        game.makeMove(move2);

        assertEquals(2, game.getMoveHistory().size(), "应该有2条移动记录");
        assertEquals(4, game.getGrid().getCell(0, 0).getValue());
        assertEquals(5, game.getGrid().getCell(0, 1).getValue());

        // 重置游戏
        game.resetToInitialState();

        // 验证所有移动都被撤销
        assertEquals(0, game.getGrid().getCell(0, 0).getValue(), "位置(0,0)应该恢复空值");
        assertEquals(0, game.getGrid().getCell(0, 1).getValue(), "位置(0,1)应该恢复空值");
        assertEquals(0, game.getMoveHistory().size(), "移动历史应该被清空");
    }

    @Test
    void testResetAfterUndoRedo() {
        // 进行移动、撤销、重做的复杂序列
        Move move = new Move(0, 0, 4, null);

        // 移动
        game.makeMove(move);
        assertEquals(1, game.getMoveHistory().size());
        assertEquals(0, game.getRedoHistory().size());

        // 撤销
        game.undoMove();
        assertEquals(0, game.getMoveHistory().size());
        assertEquals(1, game.getRedoHistory().size());

        // 重做
        game.redoMove();
        assertEquals(1, game.getMoveHistory().size());
        assertEquals(0, game.getRedoHistory().size());

        // 重置
        game.resetToInitialState();
        assertEquals(0, game.getMoveHistory().size());
        assertEquals(0, game.getRedoHistory().size());
        assertEquals(0, game.getGrid().getCell(0, 0).getValue());
    }

    @Test
    void testResetGameStatus() {
        // 模拟游戏失败状态
        game.setStatus(GameStatus.FAILED);
        game.setEndTime(LocalDateTime.now());

        // 重置游戏
        game.resetToInitialState();

        // 验证状态被重置
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertNull(game.getEndTime());
    }

    @Test
    void testResetWithNullInitialGrid() {
        // 创建一个没有初始棋盘的游戏（通过反射或直接设置）
        game.setInitialGrid(null);

        // 尝试重置应该抛出异常
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> game.resetToInitialState(),
                "应该抛出IllegalStateException"
        );

        assertTrue(exception.getMessage().contains("初始棋盘状态未保存"));
    }

    @Test
    void testResetDoesNotAffectInitialGrid() {
        // 获取初始棋盘的引用
        SudokuGrid initialGrid = game.getInitialGrid();

        // 进行移动
        game.makeMove(new Move(0, 0, 4, null));

        // 重置游戏
        game.resetToInitialState();

        // 验证初始棋盘没有被修改
        assertSame(initialGrid, game.getInitialGrid(), "初始棋盘引用应该不变");
        assertEquals(0, initialGrid.getCell(0, 0).getValue(), "初始棋盘内容不应该被修改");
    }
} 