# 数独乐乐 (Sudoku-LeLe) version 1.0

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**数独乐乐** 是一个基于Java Spring Boot构建的现代化、功能强大的数独Web应用。它旨在通过提供智能提示和友好的交互方式，帮助数独初学者和爱好者提升解题技巧。本项目是对现有数独项目进行逆向分析后，根据新的业务愿景升级改造而成。

> 本项目集成了来自 [SudokuWiki.org](https://www.sudokuwiki.org/) 的解题策略和谜题资源，为用户提供持续提升的解题算法和验证支持。

## ✨ 主要功能

- **智能下一步提示**: 当你遇到困难时，可以请求提示。系统不仅会告诉你下一步可以填哪个数字，还会高亮相关的单元格，并明确告诉你使用了哪种高级解题策略（如“唯一候选数”、“隐性配对”等）。
- **探索与回溯**: 游戏支持完整的撤销（Undo）和重做（Redo）功能。你可以在遇到不确定的分支点时大胆尝试，并随时能回到之前的任何一步。
- **URL谜题导入**: 无需手动输入题目。直接复制 [SudokuWiki.org](https://www.sudokuwiki.org/) 上的谜题页面URL，即可在应用内一键导入，方便快捷。
- **灵活的策略集成**: 后端采用策略模式设计，可以轻松、独立地引入新的解题算法，并确保新旧算法的正确性和性能不受影响。

## 🛠️ 技术栈

- **后端**: Java 17, Spring Boot, Spring MVC, Spring Data JPA
- **数据库**: H2 (内存数据库，易于启动), MySQL/PostgreSQL (生产环境)
- **构建工具**: Apache Maven
- **辅助库**: Lombok, Jsoup (用于解析URL)

## 🚀 快速开始

在运行本项目前，请确保你的开发环境满足以下要求。

### **环境要求**

- Git
- JDK 17 或更高版本
- Apache Maven 3.8.x 或更高版本

### **启动步骤**

1.  **克隆仓库**
    ```bash
    git clone [https://github.com/your-username/sudoku-lele.git](https://github.com/your-username/sudoku-lele.git)
    cd sudoku-lele
    ```

2.  **使用Maven运行项目**
    ```bash
    mvn spring-boot:run
    ```

3.  **访问应用**
    启动成功后，在浏览器中打开 `http://localhost:8080` 即可开始游戏。

## 📖 API 接口概览

本项目采用前后端分离架构，后端提供以下核心RESTful API：

- `GET /api/games`: 创建一局新游戏（使用默认谜题）。
- `POST /api/games/import-string`: 从81位字符串创建新游戏。
- `POST /api/games/import-url`: 从SudokuWiki URL创建新游戏。
- `PUT /api/games/{gameId}/cell`: 提交一步操作（填写/清除数字）。
- `POST /api/games/{gameId}/undo`: 撤销上一步操作。
- `POST /api/games/{gameId}/redo`: 重做已撤销的操作。
- `POST /api/games/{gameId}/reset`: 重置游戏到初始状态。
- `GET /api/games/{gameId}/hint`: 获取下一步提示。

## 🤝 如何贡献

我们欢迎任何形式的贡献！如果你想参与本项目，请遵循标准的 **Fork & Pull Request** 流程：

1.  **Fork** 本仓库到你自己的GitHub账户。
2.  将你Fork的仓库 **Clone** 到你的本地电脑。
3.  为你需要修改的内容创建一个新的 **Branch** (分支)。
4.  在新的分支上进行修改和提交。
5.  将你的分支 **Push** 到你Fork的远程仓库。
6.  创建一个 **Pull Request**，请求将你的分支合并到本项目的主分支中。

请确保你的代码遵循项目现有的编码风格，并为你的PR提供清晰的描述。

## 📝 许可证

本项目采用 [MIT License](LICENSE) 开源许可证。