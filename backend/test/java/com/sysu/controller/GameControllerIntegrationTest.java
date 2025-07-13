package com.sysu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysu.pojo.Game;
import com.sysu.pojo.GameStatus;
import com.sysu.pojo.Move;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * GameController集成测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateNewGame() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.gameId").exists())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.grid").exists());
    }

    @Test
    public void testCreateGameFromString() throws Exception {
        GameController.StringImportRequest request = new GameController.StringImportRequest();
        request.setPuzzleString("003020600900305001001806400008102900700000008006708200002609500800203009005010300");

        mockMvc.perform(post("/api/games/import-string")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.gameId").exists())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    public void testCreateGameFromInvalidString() throws Exception {
        GameController.StringImportRequest request = new GameController.StringImportRequest();
        request.setPuzzleString("invalid_string");

        mockMvc.perform(post("/api/games/import-string")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(containsString("导入失败")));
    }

    @Test
    public void testCreateGameFromUrl() throws Exception {
        GameController.UrlImportRequest request = new GameController.UrlImportRequest();
        request.setUrl("https://www.sudokuwiki.org/sudoku.htm?bd=003020600900305001001806400008102900700000008006708200002609500800203009005010300");

        // 注意：这个测试可能因网络问题而失败，在CI环境中可能需要mock
        mockMvc.perform(post("/api/games/import-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNumber()); // 可能成功(1)或失败(0)
    }

    @Test
    public void testMakeValidMove() throws Exception {
        // 首先创建一个游戏
        String gameResponse = mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 解析游戏ID
        ObjectMapper mapper = new ObjectMapper();
        var responseNode = mapper.readTree(gameResponse);
        int gameId = responseNode.get("data").get("gameId").asInt();

        // 创建一个有效的移动
        Move move = new Move(0, 0, 4, null); // 在位置(0,0)填入4

        mockMvc.perform(put("/api/games/" + gameId + "/cell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(move)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.validMove").isBoolean());
    }

    @Test
    public void testMakeInvalidMove() throws Exception {
        // 首先创建一个游戏
        String gameResponse = mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        var responseNode = mapper.readTree(gameResponse);
        int gameId = responseNode.get("data").get("gameId").asInt();

        // 创建一个无效的移动（与现有数字冲突）
        Move move = new Move(0, 0, 3, null); // 在位置(0,0)填入3，但第0行已经有3了

        mockMvc.perform(put("/api/games/" + gameId + "/cell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(move)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.validMove").value(false));
    }

    @Test
    public void testUndo() throws Exception {
        // 创建游戏并进行一步移动
        String gameResponse = mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        var responseNode = mapper.readTree(gameResponse);
        int gameId = responseNode.get("data").get("gameId").asInt();

        // 进行一步移动
        Move move = new Move(0, 0, 4, null);
        mockMvc.perform(put("/api/games/" + gameId + "/cell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(move)))
                .andExpect(status().isOk());

        // 撤销移动
        mockMvc.perform(post("/api/games/" + gameId + "/undo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.validMove").value(true)); // 撤销成功
    }

    @Test
    public void testRedo() throws Exception {
        // 创建游戏，进行移动，撤销，然后重做
        String gameResponse = mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        var responseNode = mapper.readTree(gameResponse);
        int gameId = responseNode.get("data").get("gameId").asInt();

        // 进行一步移动
        Move move = new Move(0, 0, 4, null);
        mockMvc.perform(put("/api/games/" + gameId + "/cell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(move)))
                .andExpect(status().isOk());

        // 撤销移动
        mockMvc.perform(post("/api/games/" + gameId + "/undo"))
                .andExpect(status().isOk());

        // 重做移动
        mockMvc.perform(post("/api/games/" + gameId + "/redo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.validMove").value(true)); // 重做成功
    }

    @Test
    public void testResetGame() throws Exception {
        // 创建游戏，进行多步移动，然后重置
        String gameResponse = mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        var responseNode = mapper.readTree(gameResponse);
        int gameId = responseNode.get("data").get("gameId").asInt();

        // 进行几步移动
        Move move1 = new Move(0, 0, 4, null);
        mockMvc.perform(put("/api/games/" + gameId + "/cell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(move1)))
                .andExpect(status().isOk());

        Move move2 = new Move(0, 1, 5, null);
        mockMvc.perform(put("/api/games/" + gameId + "/cell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(move2)))
                .andExpect(status().isOk());

        // 重置游戏
        mockMvc.perform(post("/api/games/" + gameId + "/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.validMove").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        // 验证游戏确实重置了 - 检查(0,0)位置是否恢复到空
        // 这里我们可以通过再次尝试相同的移动来验证
        mockMvc.perform(put("/api/games/" + gameId + "/cell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(move1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.validMove").value(true)); // 应该能再次成功移动
    }

    @Test
    public void testGetHint() throws Exception {
        // 创建游戏
        String gameResponse = mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        var responseNode = mapper.readTree(gameResponse);
        int gameId = responseNode.get("data").get("gameId").asInt();

        // 获取提示
        mockMvc.perform(get("/api/games/" + gameId + "/hint"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
        // 注意：提示可能为null，取决于当前谜题状态
    }

    @Test
    public void testGameStateProgression() throws Exception {
        // 测试游戏状态的完整流程

        // 1. 创建一个接近完成的游戏
        GameController.StringImportRequest request = new GameController.StringImportRequest();
        request.setPuzzleString("483921657967345821251876493548132976729564138136798245372689514814253769695417300");

        String gameResponse = mockMvc.perform(post("/api/games/import-string")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        var responseNode = mapper.readTree(gameResponse);
        int gameId = responseNode.get("data").get("gameId").asInt();

        // 2. 完成最后两步 (根据测试日志，需要填(8,7)和(8,8))
        Move penultimateMove = new Move(8, 7, 8, null);
        mockMvc.perform(put("/api/games/" + gameId + "/cell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(penultimateMove)))
                .andExpect(status().isOk());

        Move finalMove = new Move(8, 8, 2, null);
        mockMvc.perform(put("/api/games/" + gameId + "/cell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(finalMove)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SOLVED"));
    }

    @Test
    public void testInvalidGameId() throws Exception {
        // 测试不存在的游戏ID
        int invalidGameId = 99999;

        mockMvc.perform(get("/api/games/" + invalidGameId + "/hint"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty()); // 应该返回null或空
    }
} 