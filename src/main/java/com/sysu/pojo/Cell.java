package com.sysu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 代表棋盘上的一个单元格。
 */
@Data
@AllArgsConstructor
public class Cell {

    private final int row;
    private final int col;
    private Integer value; // 当前值, null或0表示为空, 在提交走子操作中，0或null表示要清除当前单元格的值
    private final boolean isGiven; // 是否为谜题初始给定的数字

    // 存储候选数，用于提示功能和用户标记 [2,8]
    private Set<Integer> candidates;

    // 深拷贝单元格
    @Override
    public Cell clone() {
        try {
            Cell newCell = (Cell) super.clone();
            if (this.candidates != null) {
                newCell.candidates = new HashSet<>(this.candidates);
            }
            return newCell;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}