package com.sysu.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数独谜题导入服务
 * 负责从各种来源导入数独谜题
 */
@Service
public class PuzzleImportService {
    
    /**
     * 从SudokuWiki URL导入谜题
     * @param url SudokuWiki的URL，格式如：https://www.sudokuwiki.org/sudoku.htm?bd=...
     * @return 81位数独字符串，空格用'0'表示
     * @throws IllegalArgumentException 如果URL格式不正确
     * @throws IOException 如果网络访问失败
     */
    public String importFromSudokuWikiUrl(String url) throws IOException {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL不能为空");
        }
        
        // 检查是否是SudokuWiki URL
        if (!url.contains("sudokuwiki.org")) {
            throw new IllegalArgumentException("只支持SudokuWiki.org的URL");
        }
        
        // 尝试从URL参数中直接提取bd参数
        String puzzleString = extractBdParameter(url);
        if (puzzleString != null) {
            return puzzleString;
        }
        
        // 如果无法从URL参数提取，尝试访问页面并解析
        return fetchPuzzleFromPage(url);
    }
    
    /**
     * 从URL参数中提取bd参数
     */
    private String extractBdParameter(String url) {
        try {
            // 匹配bd参数
            Pattern pattern = Pattern.compile("[?&]bd=([^&]+)");
            Matcher matcher = pattern.matcher(url);
            
            if (matcher.find()) {
                String bdValue = matcher.group(1);
                // URL解码
                bdValue = URLDecoder.decode(bdValue, StandardCharsets.UTF_8);
                
                // 验证并转换格式
                return validateAndConvertPuzzleString(bdValue);
            }
        } catch (Exception e) {
            // 如果提取失败，返回null，让后续方法处理
            return null;
        }
        return null;
    }
    
    /**
     * 访问页面并解析数独谜题
     */
    private String fetchPuzzleFromPage(String url) throws IOException {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            // 尝试从页面中查找数独数据
            // SudokuWiki可能将数据存储在JavaScript变量或特定的HTML元素中
            String pageText = doc.text();
            
            // 查找81位数字字符串模式
            Pattern pattern = Pattern.compile("([0-9\\.]{81})");
            Matcher matcher = pattern.matcher(pageText);
            
            if (matcher.find()) {
                String puzzleString = matcher.group(1);
                return validateAndConvertPuzzleString(puzzleString);
            }
            
            throw new IllegalArgumentException("无法从页面中提取数独谜题数据");
            
        } catch (IOException e) {
            throw new IOException("访问URL失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证并转换谜题字符串格式
     * @param puzzleString 原始谜题字符串
     * @return 标准化的81位字符串
     */
    private String validateAndConvertPuzzleString(String puzzleString) {
        if (puzzleString == null) {
            throw new IllegalArgumentException("谜题字符串不能为空");
        }
        
        // 移除所有非数字和非点号字符
        puzzleString = puzzleString.replaceAll("[^0-9\\.]", "");
        
        // 检查长度
        if (puzzleString.length() != 81) {
            throw new IllegalArgumentException("谜题字符串长度必须为81位，当前长度: " + puzzleString.length());
        }
        
        // 将点号转换为0
        puzzleString = puzzleString.replace('.', '0');
        
        // 验证只包含0-9的数字
        if (!puzzleString.matches("[0-9]{81}")) {
            throw new IllegalArgumentException("谜题字符串只能包含0-9的数字");
        }
        
        return puzzleString;
    }
    
    /**
     * 直接从81位字符串导入谜题
     * @param puzzleString 81位数独字符串
     * @return 验证后的谜题字符串
     */
    public String importFromString(String puzzleString) {
        return validateAndConvertPuzzleString(puzzleString);
    }
    
    /**
     * 验证谜题是否有效（基本检查）
     * @param puzzleString 81位谜题字符串
     * @return 是否有效
     */
    public boolean isValidPuzzle(String puzzleString) {
        try {
            // 验证格式
            validateAndConvertPuzzleString(puzzleString);
            
            // 检查给定数字是否符合数独规则
            for (int i = 0; i < 81; i++) {
                char ch = puzzleString.charAt(i);
                if (ch != '0') {
                    int row = i / 9;
                    int col = i % 9;
                    int value = Character.getNumericValue(ch);
                    
                    // 检查行冲突
                    for (int c = 0; c < 9; c++) {
                        if (c != col) {
                            char otherCh = puzzleString.charAt(row * 9 + c);
                            if (otherCh != '0' && Character.getNumericValue(otherCh) == value) {
                                return false;
                            }
                        }
                    }
                    
                    // 检查列冲突
                    for (int r = 0; r < 9; r++) {
                        if (r != row) {
                            char otherCh = puzzleString.charAt(r * 9 + col);
                            if (otherCh != '0' && Character.getNumericValue(otherCh) == value) {
                                return false;
                            }
                        }
                    }
                    
                    // 检查九宫格冲突
                    int boxStartRow = (row / 3) * 3;
                    int boxStartCol = (col / 3) * 3;
                    for (int r = boxStartRow; r < boxStartRow + 3; r++) {
                        for (int c = boxStartCol; c < boxStartCol + 3; c++) {
                            if (r != row || c != col) {
                                char otherCh = puzzleString.charAt(r * 9 + c);
                                if (otherCh != '0' && Character.getNumericValue(otherCh) == value) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 