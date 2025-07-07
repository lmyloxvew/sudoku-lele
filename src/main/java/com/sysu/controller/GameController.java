package com.sysu.controller;

import com.sysu.pojo.*;
import com.sysu.server.GameServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/games")
@RestController
@Slf4j
public class GameController {

    // 依赖注入
    @Autowired
    private GameServer gameServer;

    /*
    创建一个新游戏
    TODO：从网站中提取数独字符串
     */
    @GetMapping
    public Result startGame() {
        log.info("start game");

        Game newGame = gameServer.getNewGame();

        if (newGame != null) {
            return Result.success(newGame);
        } else {
            return Result.error("this game isn't exist");
        }
    }

    /*
    提交走子
     */
    @PutMapping("/{gameId}/cell")
    public Result makeMove(@PathVariable("gameId") Integer gameId, @RequestBody Move move) {
        log.info("make move");

        MoveResponse mr = gameServer.move(gameId, move);

        return Result.success(mr);
    }

    /**
     * 撤销操作
     */
    @PostMapping("/{gameId}/undo")
    public Result undoMove(@PathVariable("gameId") Integer gameId) {
        log.info("undo move");

        MoveResponse mr = gameServer.undoMove(gameId);

        return Result.success(mr);
    }

    /**
     * 重做操作
     */
    @PostMapping("/{gameId}/redo")
    public Result redoMove(@PathVariable("gameId") Integer gameId) {
        log.info("redo move");

        MoveResponse mr = gameServer.redoMove(gameId);

        return Result.success(mr);
    }

    /**
     * 获取下一步提示
     */
    @GetMapping("/{gameId}/hint")
    public Result getHint(@PathVariable("gameId") Integer gameId) {
        log.info("get hint");

        Hint ht = gameServer.getHint(gameId);

        return Result.success(ht);
    }
}
