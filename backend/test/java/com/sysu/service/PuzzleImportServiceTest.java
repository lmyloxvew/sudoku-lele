package com.sysu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 谜题导入服务单元测试
 */
public class PuzzleImportServiceTest {

    private PuzzleImportService puzzleImportService;

    @BeforeEach
    void setUp() {
        puzzleImportService = new PuzzleImportService();
    }

    @Test
    void testImportFromString_ValidPuzzle() {
        // 测试有效的谜题字符串
        String validPuzzle = "003020600900305001001806400008102900700000008006708200002609500800203009005010300";

        String result = puzzleImportService.importFromString(validPuzzle);

        assertEquals(validPuzzle, result);
        assertEquals(81, result.length());
    }

    @Test
    void testImportFromString_WithDots() {
        // 测试包含点号的谜题字符串
        String puzzleWithDots = "..3.2.6..9..3.5..1..18.64....81.29..7.......8..67.82....26.95..8..2.3..9..5.1.3..";

        String result = puzzleImportService.importFromString(puzzleWithDots);
        String expected = "003020600900305001001806400008102900700000008006708200002609500800203009005010300";

        assertEquals(expected, result);
    }

    @Test
    void testImportFromString_InvalidLength() {
        // 测试长度不正确的字符串
        String shortPuzzle = "00302060090030500100180640";

        assertThrows(IllegalArgumentException.class, () -> {
            puzzleImportService.importFromString(shortPuzzle);
        });
    }

    @Test
    void testImportFromString_InvalidCharacters() {
        // 测试包含无效字符的字符串
        String invalidPuzzle = "003020600900305001001806400008102900700000008006708200002609500800203009005010abc";

        assertThrows(IllegalArgumentException.class, () -> {
            puzzleImportService.importFromString(invalidPuzzle);
        });
    }

    @Test
    void testImportFromString_NullInput() {
        // 测试空输入
        assertThrows(IllegalArgumentException.class, () -> {
            puzzleImportService.importFromString(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            puzzleImportService.importFromString("");
        });
    }

    @Test
    void testIsValidPuzzle_ValidPuzzle() {
        // 测试有效的数独
        String validPuzzle = "003020600900305001001806400008102900700000008006708200002609500800203009005010300";

        assertTrue(puzzleImportService.isValidPuzzle(validPuzzle));
    }

    @Test
    void testIsValidPuzzle_InvalidPuzzle_RowConflict() {
        // 测试行冲突的数独
        String invalidPuzzle = "113020600900305001001806400008102900700000008006708200002609500800203009005010300";

        assertFalse(puzzleImportService.isValidPuzzle(invalidPuzzle));
    }

    @Test
    void testIsValidPuzzle_InvalidPuzzle_ColumnConflict() {
        // 测试列冲突的数独
        String invalidPuzzle = "003020600900305001001806400008102900700000008006708200002609500800203009905010300";

        assertFalse(puzzleImportService.isValidPuzzle(invalidPuzzle));
    }

    @Test
    void testIsValidPuzzle_InvalidPuzzle_BoxConflict() {
        // 测试九宫格冲突的数独
        String invalidPuzzle = "303020600900305001001806400008102900700000008006708200002609500800203009005010300";

        assertFalse(puzzleImportService.isValidPuzzle(invalidPuzzle));
    }

    @Test
    void testIsValidPuzzle_CompletedPuzzle() {
        // 测试已完成的数独
        String completedPuzzle = "483921657967345821251876493548132976729564138136798245372689514814253769695417382";

        assertTrue(puzzleImportService.isValidPuzzle(completedPuzzle));
    }

    @Test
    void testImportFromSudokuWikiUrl_InvalidUrl() {
        // 测试无效的URL
        assertThrows(IllegalArgumentException.class, () -> {
            puzzleImportService.importFromSudokuWikiUrl(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            puzzleImportService.importFromSudokuWikiUrl("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            puzzleImportService.importFromSudokuWikiUrl("https://www.google.com");
        });
    }

    @Test
    void testImportFromSudokuWikiUrl_WithBdParameter() {
        // 测试包含bd参数的URL（模拟情况）
        String urlWithBd = "https://www.sudokuwiki.org/sudoku.htm?bd=003020600900305001001806400008102900700000008006708200002609500800203009005010300";

        try {
            String result = puzzleImportService.importFromSudokuWikiUrl(urlWithBd);
            assertEquals(81, result.length());
            assertTrue(result.matches("[0-9]{81}"));
        } catch (Exception e) {
            // 如果网络访问失败，这是预期的
            assertTrue(e.getMessage().contains("访问URL失败") || e.getMessage().contains("无法从页面中提取"));
        }
    }

    @Test
    void testValidateAndConvertPuzzleString_WithSpaces() {
        // 测试包含空格的字符串处理
        String puzzleWithSpaces = "0 0 3 0 2 0 6 0 0 9 0 0 3 0 5 0 0 1 0 0 1 8 0 6 4 0 0 0 0 8 1 0 2 9 0 0 7 0 0 0 0 0 0 0 8 0 0 6 7 0 8 2 0 0 0 0 2 6 0 9 5 0 0 8 0 0 2 0 3 0 0 9 0 0 5 0 1 0 3 0 0";

        // 移除空格后应该能正常处理
        String expectedResult = "003020600900305001001806400008102900700000008006708200002609500800203009005010300";
        String result = puzzleImportService.importFromString(puzzleWithSpaces.replaceAll("\\s", ""));

        assertEquals(expectedResult, result);
    }

    @Test
    void testValidateAndConvertPuzzleString_EdgeCases() {
        // 测试边界情况

        // 全空的数独
        String emptyPuzzle = "000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        assertTrue(puzzleImportService.isValidPuzzle(emptyPuzzle));

        // 只有一个数字的数独
        String singleNumberPuzzle = "100000000000000000000000000000000000000000000000000000000000000000000000000000000";
        assertTrue(puzzleImportService.isValidPuzzle(singleNumberPuzzle));
    }
} 