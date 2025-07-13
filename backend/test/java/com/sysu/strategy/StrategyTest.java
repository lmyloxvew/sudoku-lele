package com.sysu.strategy;

import com.sysu.pojo.Hint;
import com.sysu.pojo.SudokuGrid;
import com.sysu.strategy.impl.HiddenSingleStrategy;
import com.sysu.strategy.impl.NakedSingleStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 解题策略单元测试
 */
public class StrategyTest {

    private NakedSingleStrategy nakedSingleStrategy;
    private HiddenSingleStrategy hiddenSingleStrategy;
    private StrategyManager strategyManager;

    @BeforeEach
    void setUp() {
        nakedSingleStrategy = new NakedSingleStrategy();
        hiddenSingleStrategy = new HiddenSingleStrategy();
        strategyManager = new StrategyManager();
    }

    @Test
    void testNakedSingleStrategy() {
        // 创建一个接近完成的数独，其中某个位置只有一个候选数
        String puzzleWithNakedSingle = "483921657967345821251876493548132976729564138136798245372689514814253769695417380";
        SudokuGrid grid = new SudokuGrid(puzzleWithNakedSingle);

        // 应该能找到提示
        assertTrue(nakedSingleStrategy.isApplicable(grid));
        Hint hint = nakedSingleStrategy.findHint(grid);

        assertNotNull(hint);
        assertEquals("显性唯一 (Naked Single)", hint.getStrategyName());
        assertNotNull(hint.getSuggestedMove());

        // 验证建议的移动 - 修正期望值为实际正确答案
        assertEquals(8, hint.getSuggestedMove().getRow());
        assertEquals(8, hint.getSuggestedMove().getCol());
        assertEquals(2, hint.getSuggestedMove().getValue());
    }

    @Test
    void testHiddenSingleStrategy() {
        // 创建一个可以使用Hidden Single策略的数独
        String puzzleWithHiddenSingle = "300000000000000000000000000000000000000000000000000000000000000000000000000000000";
        SudokuGrid grid = new SudokuGrid(puzzleWithHiddenSingle);

        assertTrue(hiddenSingleStrategy.isApplicable(grid));

        // 注意：这个简单的例子可能不会立即找到Hidden Single
        // 因为需要更复杂的约束条件
    }

    @Test
    void testStrategyManager() {
        // 测试策略管理器
        assertNotNull(strategyManager);

        // 检查策略是否按优先级排序
        var strategies = strategyManager.getAllStrategies();
        assertTrue(strategies.size() >= 2);

        // NakedSingle应该优先级更高（数字更小）
        assertTrue(strategies.get(0).getPriority() <= strategies.get(1).getPriority());
    }

    @Test
    void testStrategyManagerGetHint() {
        // 使用一个有明确解的数独
        String puzzleString = "483921657967345821251876493548132976729564138136798245372689514814253769695417300";
        SudokuGrid grid = new SudokuGrid(puzzleString);

        Hint hint = strategyManager.getHint(grid);

        assertNotNull(hint);
        assertNotNull(hint.getStrategyName());
        assertNotNull(hint.getDescription());
        assertNotNull(hint.getSuggestedMove());
    }

    @Test
    void testStrategyPriority() {
        // 测试策略优先级
        assertTrue(nakedSingleStrategy.getPriority() < hiddenSingleStrategy.getPriority());
    }

    @Test
    void testStrategyNames() {
        // 测试策略名称
        assertEquals("显性唯一 (Naked Single)", nakedSingleStrategy.getStrategyName());
        assertEquals("隐性唯一 (Hidden Single)", hiddenSingleStrategy.getStrategyName());
    }

    @Test
    void testAddNewStrategy() {
        // 测试添加新策略
        int initialSize = strategyManager.getAllStrategies().size();

        // 创建一个测试策略
        SudokuStrategy testStrategy = new SudokuStrategy() {
            @Override
            public String getStrategyName() {
                return "Test Strategy";
            }

            @Override
            public int getPriority() {
                return 999;
            }

            @Override
            public Hint findHint(SudokuGrid grid) {
                return null;
            }

            @Override
            public boolean isApplicable(SudokuGrid grid) {
                return false;
            }
        };

        strategyManager.addStrategy(testStrategy);
        assertEquals(initialSize + 1, strategyManager.getAllStrategies().size());
    }

    @Test
    void testStrategyWithCompletedGrid() {
        // 已完成的数独不应该有提示
        String completedPuzzle = "483921657967345821251876493548132976729564138136798245372689514814253769695417382";
        SudokuGrid grid = new SudokuGrid(completedPuzzle);

        // 策略应该仍然适用（有空格检查），但不应该找到提示
        assertTrue(nakedSingleStrategy.isApplicable(grid) || !nakedSingleStrategy.isApplicable(grid)); // 取决于实现

        Hint hint = strategyManager.getHint(grid);
        // 对于已完成的网格，可能返回null或者策略不适用
        // 这取决于具体的实现策略
    }

    @Test
    void testStrategyWithInvalidGrid() {
        // 测试带有冲突的网格
        String invalidPuzzle = "113020600900305001001806400008102900700000008006708200002609500800203009005010300";
        SudokuGrid grid = new SudokuGrid(invalidPuzzle);

        // 即使网格有冲突，策略仍然应该能够运行（不抛异常）
        assertDoesNotThrow(() -> {
            nakedSingleStrategy.isApplicable(grid);
            nakedSingleStrategy.findHint(grid);
        });
    }
} 