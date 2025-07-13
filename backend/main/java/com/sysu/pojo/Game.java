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

    public static java.util.concurrent.atomic.AtomicInteger idGenerator = new java.util.concurrent.atomic.AtomicInteger(0);

    private Integer gameId; // 游戏唯一标识符
    private SudokuGrid grid; // 玩家操作的当前布局
    private SudokuGrid initialGrid; // 初始棋盘状态（用于重置）
    private GameStatus status; // 游戏状态 (进行中, 已解决等)

    // 使用栈来实现操作历史，用于撤销和重做
    private Stack<Move> moveHistory;
    private Stack<Move> redoHistory;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 兼容性构造函数
    public Game(Integer gameId, SudokuGrid grid, GameStatus status, Stack<Move> moveHistory, Stack<Move> redoHistory, LocalDateTime startTime, LocalDateTime endTime) {
        this.gameId = gameId;
        this.grid = grid;
        this.initialGrid = grid.clone(); // 保存初始状态的深拷贝
        this.status = status;
        this.moveHistory = moveHistory;
        this.redoHistory = redoHistory;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * 重置游戏到初始状态
     * 清除所有玩家的移动，恢复到谜题的原始状态
     */
    public void resetToInitialState() {
        if (initialGrid == null) {
            throw new IllegalStateException("初始棋盘状态未保存，无法重置");
        }
        
        // 恢复棋盘到初始状态
        this.grid = initialGrid.clone();
        
        // 清空移动历史
        this.moveHistory.clear();
        this.redoHistory.clear();
        
        // 重置游戏状态
        this.status = GameStatus.IN_PROGRESS;
        this.endTime = null;
        
        // 更新候选数
        this.grid.updateAllCandidates();
    }

    /**
     * 执行一步操作
     * @param move 要执行的操作
     * @return 操作是否合法
     */
    public boolean makeMove(Move move) {
        // 输入验证
        if (move == null) {
            throw new IllegalArgumentException("移动操作不能为空");
        }
        
        if (move.getRow() < 0 || move.getRow() >= 9 || move.getCol() < 0 || move.getCol() >= 9) {
            throw new IndexOutOfBoundsException("坐标超出范围: (" + move.getRow() + "," + move.getCol() + ")");
        }
        
        // 验证逻辑 (例如: 不能修改初始给定的数字)
        Cell cell = grid.getCell(move.getRow(), move.getCol());
        if (cell.isGiven()) {
            return false;
        }

        // 如果是清除操作（value为0或null）
        if (move.getValue() == null || move.getValue() == 0) {
            move.setPreviousValue(cell.getValue());
            cell.setValue(0);
            moveHistory.push(move);
            redoHistory.clear();
            
            // 更新所有候选数
            grid.updateAllCandidates();
            updateStatus();
            return true;
        }

        // 验证移动是否合法
        if (!grid.isValidMove(move.getRow(), move.getCol(), move.getValue())) {
            return false;
        }

        // 执行操作
        move.setPreviousValue(cell.getValue());
        cell.setValue(move.getValue());
        moveHistory.push(move);
        redoHistory.clear(); // 新操作会清空重做历史

        // 更新候选数和游戏状态
        grid.updateAllCandidates();
        updateStatus();
        return true;
    }

    /**
     * 更新游戏状态
     */
    private void updateStatus() {
        if (grid.hasConflicts()) {
            this.status = GameStatus.FAILED;
        } else if (grid.isCompleted()) {
            this.status = GameStatus.SOLVED;
            this.endTime = LocalDateTime.now();
        } else {
            this.status = GameStatus.IN_PROGRESS;
        }
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
            
            // 更新候选数和游戏状态
            grid.updateAllCandidates();
            updateStatus();
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

            // 4. 更新候选数和游戏状态
            grid.updateAllCandidates();
            updateStatus();

            return true;
        } else {
            // 没有可以重做的操作
            return false;
        }
    }

}