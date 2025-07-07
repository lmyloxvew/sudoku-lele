package com.sysu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 封装一次用户操作，用于实现撤销/重做功能。
 */
@Data
@AllArgsConstructor
public class Move {

    private final int row;
    private final int col;
    private final Integer value;
    private Integer previousValue;

}