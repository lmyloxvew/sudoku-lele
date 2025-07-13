package com.sysu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveResponse {
    public boolean isValidMove;
    private SudokuGrid grid;
    private GameStatus status;
}
