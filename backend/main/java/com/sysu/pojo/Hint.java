package com.sysu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 封装一个提示信息，由解题策略生成。
 */
@Data
@AllArgsConstructor
public class Hint {

    private final String strategyName; // 策略名称
    private final String description; // 对该提示的文字描述

    // 相关的单元格，用于前端高亮显示
    private final List<CellPosition> primaryCells;

    // 建议的走子
    private final Move suggestedMove;

    // 该策略能够排除的候选数
    private final Map<CellPosition, Set<Integer>> eliminations;

    // 内部记录，用于方便地传递坐标
    public record CellPosition(int row, int col) {}

}