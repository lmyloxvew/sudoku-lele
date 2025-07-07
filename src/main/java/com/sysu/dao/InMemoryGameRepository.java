package com.sysu.dao;

import com.sysu.pojo.Game;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGameRepository {

    // 使用ConcurrentHashMap来确保多线程环境下的安全
    private static final Map<Integer, Game> gameStore = new ConcurrentHashMap<>();

    /**
     * 保存一个新的或更新一个已存在的Game对象。
     * @param game 要存储的游戏对象
     */
    public void save(Game game) {
        if (game != null && game.getGameId() != null) {
            gameStore.put(game.getGameId(), game);
        }
    }

    /**
     * 利用 gameId 来寻找指定的Game对象。
     * @param gameId 游戏的唯一ID
     * @return 如果找到，则返回一个包含Game对象的Optional；否则返回空的Optional。
     */
    public Optional<Game> findById(int gameId) {
        return Optional.ofNullable(gameStore.get(gameId));
    }

    /**
     * 根据ID删除一个游戏（例如，游戏结束后清理）
     * @param gameId 游戏ID
     */
    public void deleteById(int gameId) {
        gameStore.remove(gameId);
    }
}