package com.sysu.server.impl;

import com.sysu.dao.InMemoryGameRepository;
import com.sysu.pojo.*;
import com.sysu.server.GameServer;
import com.sysu.service.PuzzleImportService;
import com.sysu.strategy.StrategyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Stack;

@Service
public class GameServerImpl implements GameServer {

    @Autowired
    InMemoryGameRepository inMemoryGameRepository;
    
    @Autowired
    StrategyManager strategyManager;
    
    @Autowired
    PuzzleImportService puzzleImportService;

    @Override
    public Game getNewGame() {

        // 固定的谜题字符串
        /*
        TODO: 从网站中获取谜题字符串以初始化棋盘
         */
        String puzzleString = "003020600900305001001806400008102900700000008006708200002609500800203009005010300";

        // 构造需要创建Game对象的元素
        int gameId = Game.id++;
        SudokuGrid grid = new SudokuGrid(puzzleString);
        GameStatus status = GameStatus.IN_PROGRESS;

        inMemoryGameRepository.save(new Game(gameId, grid, status, new Stack<>(), new Stack<>(), LocalDateTime.now(), null));

        return inMemoryGameRepository.findById(gameId).isPresent() ? inMemoryGameRepository.findById(gameId).get() : null;
    }
    
    @Override
    public Game getNewGameFromUrl(String url) throws Exception {
        // 从URL导入谜题字符串
        String puzzleString = puzzleImportService.importFromSudokuWikiUrl(url);
        return createGameFromPuzzleString(puzzleString);
    }
    
    @Override
    public Game getNewGameFromString(String puzzleString) {
        // 验证并导入谜题字符串
        String validatedPuzzleString = puzzleImportService.importFromString(puzzleString);
        return createGameFromPuzzleString(validatedPuzzleString);
    }
    
    /**
     * 从谜题字符串创建游戏
     */
    private Game createGameFromPuzzleString(String puzzleString) {
        // 构造需要创建Game对象的元素
        int gameId = Game.id++;
        SudokuGrid grid = new SudokuGrid(puzzleString);
        GameStatus status = GameStatus.IN_PROGRESS;
        
        Game game = new Game(gameId, grid, status, new Stack<>(), new Stack<>(), LocalDateTime.now(), null);
        inMemoryGameRepository.save(game);
        
        return inMemoryGameRepository.findById(gameId).orElse(null);
    }

    /**
     * 提交走子
     * TODO: get方法有获取空game的风险
     */
    @Override
    public MoveResponse move(Integer gameId, Move move) {
        Game game = inMemoryGameRepository.findById(gameId).get();

        boolean isValid = game.makeMove(move);

        // 保存已更新的游戏
        inMemoryGameRepository.save(game);
        return new MoveResponse(isValid, game.getGrid(), game.getStatus());
    }

    /**
     * 撤销操作
     * TODO: 与提交走子操作一样，需要全局异常处理器来处理异常
     */
    @Override
    public MoveResponse undoMove(Integer gameId) {
        Game game = inMemoryGameRepository.findById(gameId).get();

        boolean isNotEmpty = game.undoMove();

        inMemoryGameRepository.save(game);
        return new MoveResponse(isNotEmpty, game.getGrid(), game.getStatus());
    }

    /**
     * 重做操作
     */
    @Override
    public MoveResponse redoMove(Integer gameId) {
        Game game = inMemoryGameRepository.findById(gameId).get();

        boolean isNotEmpty = game.redoMove();

        inMemoryGameRepository.save(game);
        return new MoveResponse(isNotEmpty, game.getGrid(), game.getStatus());
    }

    /**
     * 获取下一步提示
     */
    @Override
    public Hint getHint(Integer gameId) {
        Game game = inMemoryGameRepository.findById(gameId).orElse(null);
        if (game == null) {
            return null;
        }
        
        return strategyManager.getHint(game.getGrid());
    }

}
