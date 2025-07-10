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
    创建一个新游戏（使用默认谜题）
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
    从URL创建新游戏
     */
    @PostMapping("/import-url")
    public Result startGameFromUrl(@RequestBody UrlImportRequest request) {
        log.info("start game from url: {}", request.getUrl());
        
        try {
            Game newGame = gameServer.getNewGameFromUrl(request.getUrl());
            if (newGame != null) {
                return Result.success(newGame);
            } else {
                return Result.error("创建游戏失败");
            }
        } catch (Exception e) {
            log.error("从URL导入游戏失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }
    
    /*
    从字符串创建新游戏
     */
    @PostMapping("/import-string")
    public Result startGameFromString(@RequestBody StringImportRequest request) {
        log.info("start game from string");
        
        try {
            Game newGame = gameServer.getNewGameFromString(request.getPuzzleString());
            if (newGame != null) {
                return Result.success(newGame);
            } else {
                return Result.error("创建游戏失败");
            }
        } catch (Exception e) {
            log.error("从字符串导入游戏失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }
    
    // 内部类用于接收请求参数
    public static class UrlImportRequest {
        private String url;
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
    
    public static class StringImportRequest {
        private String puzzleString;
        
        public String getPuzzleString() { return puzzleString; }
        public void setPuzzleString(String puzzleString) { this.puzzleString = puzzleString; }
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
