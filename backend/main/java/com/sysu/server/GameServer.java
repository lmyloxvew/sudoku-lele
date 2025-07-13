package com.sysu.server;


import com.sysu.pojo.Game;
import com.sysu.pojo.Hint;
import com.sysu.pojo.Move;
import com.sysu.pojo.MoveResponse;

public interface GameServer {

    // 创建新游戏
    Game getNewGame();

    // 从URL创建新游戏
    Game getNewGameFromUrl(String url) throws Exception;

    // 从字符串创建新游戏
    Game getNewGameFromString(String puzzleString);

    // 提交走子
    MoveResponse move(Integer gameId, Move move);

    // 撤销操作
    MoveResponse undoMove(Integer gameId);

    // 重做操作
    MoveResponse redoMove(Integer gameId);

    // 重置游戏到初始状态
    MoveResponse resetGame(Integer gameId);

    // 获取下一步提示
    Hint getHint(Integer gameId);
}
