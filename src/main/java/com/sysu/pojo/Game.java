package com.sysu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Stack;

/**
 * 代表一个完整的数独游戏对局。
 * 管理游戏的当前状态、历史记录和元数据。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Game {

    public static Integer id = 0;

    private Integer gameId; // 游戏唯一标识符
    private SudokuGrid grid; // 玩家操作的当前布局
    private GameStatus status; // 游戏状态 (进行中, 已解决等)

    // 使用栈来实现操作历史，用于撤销和重做
    private Stack<Move> moveHistory;
    private Stack<Move> redoHistory;

    private LocalDateTime startTime;
    private LocalDateTime endTime;


    /**
     * 执行一步操作
     * @param move 要执行的操作
     * @return 操作是否合法
     */
    public boolean makeMove(Move move) {
        // 验证逻辑 (例如: 不能修改初始给定的数字)
        // TODO: 其他验证逻辑有待补充
        Cell cell = grid.getCell(move.getRow(), move.getCol());
        if (cell.isGiven()) {
            return false;
        }

        // 执行操作
        move.setPreviousValue(cell.getValue());
        cell.setValue(move.getValue());
        moveHistory.push(move);
        redoHistory.clear(); // 新操作会清空重做历史

        // 更新游戏状态 (例如: 检查是否已解决)
        // TODO: 待实现方法
        // updateStatus();
        return true;
    }

    /**
     * 撤销上一步操作
     */
    public boolean undoMove() {
        if (!moveHistory.isEmpty()) {
            Move lastMove = moveHistory.pop();
            // 恢复单元格到上一个值
            grid.getCell(lastMove.getRow(), lastMove.getCol()).setValue(lastMove.getPreviousValue());
            redoHistory.push(lastMove);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 重做上一步被撤销的操作
     * @return 如果成功重做，返回true；否则返回false
     */
    public boolean redoMove() {
        if (!redoHistory.isEmpty()) {
            // 1. 从redo栈中取出要重做的操作
            Move moveToRedo = redoHistory.pop();

            // 2. 将操作重新应用到棋盘上
            grid.getCell(moveToRedo.getRow(), moveToRedo.getCol()).setValue(moveToRedo.getValue());

            // 3. 将操作重新放回undo历史记录中
            moveHistory.push(moveToRedo);

            return true;
        } else {
            // 没有可以重做的操作
            return false;
        }
    }

}