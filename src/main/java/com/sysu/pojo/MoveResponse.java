package com.sysu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveResponse {
    private boolean isValidMove;
    private SudokuGrid grid;
    private GameStatus status;
}
