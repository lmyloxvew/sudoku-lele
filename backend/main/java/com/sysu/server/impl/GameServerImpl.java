package com.sysu.server.impl;

import com.sysu.dao.InMemoryGameRepository;
import com.sysu.pojo.*;
import com.sysu.server.GameServer;
import com.sysu.service.PuzzleImportService;
import com.sysu.strategy.StrategyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GameServerImpl implements GameServer {

    @Autowired
    InMemoryGameRepository inMemoryGameRepository;

    @Autowired
    StrategyManager strategyManager;

    @Autowired
    PuzzleImportService puzzleImportService;

    // 随机数生成器实例
    private static final Random RANDOM = new Random();

    // 创建一个包含10个谜题字符串的静态列表
    private static final List<String> PUZZLE_STRINGS = Arrays.asList(
            "003020600900305001001806400008102900700000008006708200002609500800203009005010300",
            "200080300060070084030500209000105408000000000507208000306002010710030040004050006",
            "000000801040030000000950300000004070701000602050200000007062000000040050403000000",
            "600100005007000100040080070000005001020000030700600000030050060001000300800004009",
            "000002030020100005000040100004000000100806003000000200009030000500007080060400000",
            "043080250600000000000001094900004000050000030000500008270400000000000000089020140",
            "000000043002000100000705000000030009080000020500010000000502000004000600320000000",
            "000006000059000008000043500100000000060090000000000004000028000005000073000000400",
            "000260701680070090190004500820100040004602900050003028009300074040050036703018000",
            "030000000000190300007002000000008040002700000300000001001005004000000060080400005"
    );


    @Override
    public Game getNewGame() {

        // 从列表中随机选择一个谜题字符串
        String puzzleString = PUZZLE_STRINGS.get(RANDOM.nextInt(PUZZLE_STRINGS.size()));

        // 构造需要创建Game对象的元素
        int gameId = Game.idGenerator.incrementAndGet();
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
        int gameId = Game.idGenerator.incrementAndGet();
        SudokuGrid grid = new SudokuGrid(puzzleString);
        GameStatus status = GameStatus.IN_PROGRESS;

        Game game = new Game(gameId, grid, status, new Stack<>(), new Stack<>(), LocalDateTime.now(), null);
        inMemoryGameRepository.save(game);

        return inMemoryGameRepository.findById(gameId).orElse(null);
    }

    /**
     * 提交走子
     */
    @Override
    public MoveResponse move(Integer gameId, Move move) {
        Game game = inMemoryGameRepository.findById(gameId)
                .orElseThrow(() -> new NoSuchElementException("游戏不存在，ID: " + gameId));

        boolean isValid = game.makeMove(move);

        // 保存已更新的游戏
        inMemoryGameRepository.save(game);
        return new MoveResponse(isValid, game.getGrid(), game.getStatus());
    }

    /**
     * 撤销操作
     */
    @Override
    public MoveResponse undoMove(Integer gameId) {
        Game game = inMemoryGameRepository.findById(gameId)
                .orElseThrow(() -> new NoSuchElementException("游戏不存在，ID: " + gameId));

        boolean isNotEmpty = game.undoMove();

        inMemoryGameRepository.save(game);
        return new MoveResponse(isNotEmpty, game.getGrid(), game.getStatus());
    }

    /**
     * 重做操作
     */
    @Override
    public MoveResponse redoMove(Integer gameId) {
        Game game = inMemoryGameRepository.findById(gameId)
                .orElseThrow(() -> new NoSuchElementException("游戏不存在，ID: " + gameId));

        boolean isNotEmpty = game.redoMove();

        inMemoryGameRepository.save(game);
        return new MoveResponse(isNotEmpty, game.getGrid(), game.getStatus());
    }

    /**
     * 重置游戏到初始状态
     */
    @Override
    public MoveResponse resetGame(Integer gameId) {
        Game game = inMemoryGameRepository.findById(gameId)
                .orElseThrow(() -> new NoSuchElementException("游戏不存在，ID: " + gameId));

        game.resetToInitialState();

        inMemoryGameRepository.save(game);
        return new MoveResponse(true, game.getGrid(), game.getStatus());
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
