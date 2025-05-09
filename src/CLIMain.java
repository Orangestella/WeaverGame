// File: CLIMain.java

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Map; // 用于处理 ValidationResult 中的 LetterState

// **导入您项目中的其他类**
// 请根据您的实际包结构，导入 WeaverModel, ValidationResult, LetterState, PathFinder 类
import exceptions.InvalidWordException; // 假设 InvalidWordException 类存在
import exceptions.WordGenerationException; // 假设 WordGenerationException 类存在
// import your_package_name.WeaverModel;      // 导入 WeaverModel 类
// import your_package_name.ValidationResult; // 导入 ValidationResult 类
// import your_package_name.LetterState;    // 导入 LetterState 枚举
// import your_package_name.PathFinder;     // 导入 PathFinder 类

public class CLIMain {

    /**
     * CLI 版本 Weaver 游戏的入口方法。
     * 创建 Model，直接处理用户输入和输出，驱动游戏流程。
     *
     * @param args 命令行参数 (未使用)。
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // 用于读取用户输入
        WeaverModel model = null; // WeaverModel 实例

        try {
            System.out.println("Welcome to Weaver CLI!"); // 欢迎信息

            // 1. 创建 Model
            // 假设 WeaverModel 的构造函数会抛出 IOException (例如加载字典失败)
            model = new WeaverModel();

            // 2. 初始化第一局游戏
            // initializeGame 方法会处理 Model 的初始化和显示初始状态
            initializeGame(model);

            // 3. 运行主游戏循环
            // runGameLoop 方法包含读取输入、命令解析、调用 Model、显示状态的逻辑
            runGameLoop(model, scanner);

        } catch (IOException e) {
            // 捕获 Model 构造函数中的字典加载失败异常
            System.err.println("Error loading dictionary: " + e.getMessage());
            // e.printStackTrace(); // 调试时可以打印堆栈
        } catch (Exception e) {
            // 捕获游戏设置或主循环中可能发生的其他意外异常
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈以帮助调试
        } finally {
            // 确保 Scanner 在程序结束时被关闭
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    /**
     * Initializes a new game by calling model.initialize() and displays the initial state.
     * Handles potential WordGenerationException during initialization.
     *
     * @param model The WeaverModel instance.
     * @throws WordGenerationException if word generation fails.
     */
    private static void initializeGame(WeaverModel model) throws WordGenerationException {
        try {
            model.initialize(); // 调用 Model 的 initialize 方法
            System.out.println("\n--- New Game Started ---");
            displayGameState(model);

            System.out.println("Game started. Enter your first word.");
        } catch (WordGenerationException e) {
            System.err.println("Failed to initialize game: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Runs the main game loop, handling user input and game progression.
     * 读取用户输入，解析命令或玩家猜词，调用 Model 方法，并显示游戏状态。
     * 循环一直运行直到用户输入 'quit' 命令。
     *
     * @param model The WeaverModel instance.
     * @param scanner The Scanner for reading user input.
     */
    private static void runGameLoop(WeaverModel model, Scanner scanner) {
        boolean gameIsRunning = true; // 控制游戏主循环是否运行
        while (gameIsRunning) {
            // 只有在游戏未胜利时才提示用户输入常规的游戏词或命令
            // 如果游戏已胜利，只提示用户输入结束游戏或开始新游戏的命令
            if (!model.isWon()) { // 假设 model.isWon() getter 存在
                promptForInput(); // 显示输入提示
            } else {
                // 如果游戏已胜利，提示用户输入 'new game' 或 'quit'
                System.out.print("Game won. Enter 'new game' to play again or 'quit' to exit: ");
            }


            String inputLine = scanner.nextLine().trim(); // 读取用户输入并去除首尾空格

            // **处理退出命令**
            if (inputLine.equalsIgnoreCase("quit")) {
                gameIsRunning = false; // 设置循环标志为 false，退出循环
                System.out.println("Quitting game. Goodbye!");
                continue; // 结束当前循环迭代，跳到下一次循环条件检查 (循环条件 gameIsRunning 将为 false)
            }

            // **处理其他命令或玩家猜词 (只在游戏未胜利时处理常规游戏词)**
            if (!model.isWon()) {
                if (inputLine.equalsIgnoreCase("reset")) {
                    // **处理重置命令**
                    model.resetGame(); // 调用 Model 的 resetGame 方法
                    displayGameState(model); // 显示重置后的游戏状态 (通过 Model getter 获取)
                    // Model 的 resetGame 方法现在不发送消息，CLIMain 负责显示重置后的起始提示
                    System.out.println("Game reset. Enter your first word.");
                } else if (inputLine.equalsIgnoreCase("new game")) {
                    // **处理开始新游戏命令**
                    try {
                        // 调用 initializeGame 方法，它会调用 model.initialize 并显示初始状态
                        initializeGame(model);
                    } catch (WordGenerationException e) {
                        // 如果新游戏初始化失败 (例如找不到路径)，打印错误信息。
                        // initializeGame 方法内部已经打印了错误。
                        System.err.println("Failed to start a new game.");
                        // 可以让用户再试一次 'new game' 或输入 'quit'。
                    }
                } else if (inputLine.equalsIgnoreCase("show path")) {
                    // **处理显示完整路径命令**
                    displaySolutionPath(model); // 调用 displaySolutionPath 方法显示完整路径
                } else if (inputLine.toLowerCase().startsWith("set errors ")) {
                    // **处理设置错误显示命令 (例如 "set errors on", "set errors off")**
                    String[] parts = inputLine.split("\\s+"); // 按一个或多个空格分割命令
                    if (parts.length == 3 && parts[1].equalsIgnoreCase("errors")) {
                        if (parts[2].equalsIgnoreCase("on")) {
                            model.setShowErrorsFlag(true); // 调用 Model 的 setShowErrorsFlag 方法
                            System.out.println("Show errors enabled."); // 立即反馈给用户
                        } else if (parts[2].equalsIgnoreCase("off")) {
                            model.setShowErrorsFlag(false); // 调用 Model 的 setShowErrorsFlag 方法
                            System.out.println("Show errors disabled."); // 立即反馈给用户
                        } else {
                            displayInvalidCommand(inputLine); // 命令格式错误
                        }
                    } else {
                        displayInvalidCommand(inputLine); // 命令格式错误
                    }
                    // 设置标志后，重新显示游戏状态，以便用户看到消息显示的变化 (如果错误之前被隐藏)
                    displayGameState(model);

                }
                // TODO: 添加其他命令的处理逻辑 (例如 set random on/off)
                else if (inputLine.toLowerCase().startsWith("set random ")) {
                    // **处理设置随机词命令 (例如 "set random on", "set random off")**
                    String[] parts = inputLine.split("\\s+"); // 按一个或多个空格分割命令
                    if (parts.length == 3 && parts[1].equalsIgnoreCase("random")) {
                        if (parts[2].equalsIgnoreCase("on")) {
                            model.setRandomWordFlag(true); // 调用 Model 的 setRandomWordFlag 方法
                            System.out.println("Random words enabled. Start a new game for changes to take effect."); // 提示需要新游戏生效
                        } else if (parts[2].equalsIgnoreCase("off")) {
                            model.setRandomWordFlag(false); // 调用 Model 的 setRandomWordFlag 方法
                            System.out.println("Fixed words enabled. Start a new game for changes to take effect."); // 提示需要新游戏生效
                        } else {
                            displayInvalidCommand(inputLine); // 命令格式错误
                        }
                    } else {
                        displayInvalidCommand(inputLine); // 命令格式错误
                    }
                    // 显示当前游戏状态
                    displayGameState(model);
                }

                else {
                    // **如果输入不是识别的命令，假定是玩家猜词**
                    // 简单的长度检查，防止提交明显错误的输入
                    if (model.getTargetWord() != null && inputLine.length() == model.getTargetWord().length()) {
                        try {
                            // **调用 Model 的 tick 方法处理玩家猜词**
                            // Model.tick 方法会抛出 InvalidWordException 或 RuntimeException
                            // 假设 tick 方法返回 ValidationResult
                            ValidationResult result = model.tick(inputLine);

                            // 如果 tick 成功 (没有抛出异常)，表示输入是一个有效的游戏步骤词
                            // 显示更新后的游戏状态 (通过 Model getter 获取)
                            displayGameState(model);

                            // 根据 Model 的 showErrorsFlag 和 ValidationResult 的消息来显示提示信息
                            // ValidationResult.getMessage() 包含来自 WithWarning 的消息 (如果 showErrorsFlag 为 true 且 WithWarning 活跃的话)
                            if (model.isShowErrorsFlag() && result.getMessage() != null && !result.getMessage().isEmpty()) {
                                System.out.println("Message: " + result.getMessage());
                            } else if (!model.isShowErrorsFlag() && model.isWon()) {
                                // **特殊处理胜利消息：** 如果游戏胜利但错误显示关闭，WithWarning 不会添加消息。
                                // 在这种情况下，CLIMain 需要自己打印胜利消息。
                                System.out.println("Congratulations! You won the game!");
                            }
                            // Note: 如果 showErrorsFlag 是 OFF，来自 WithWarning 的消息 (如 "Invalid input." 或 "Valid word.") 会被抑制。
                            // 对于无效输入，CLIMain 需要在 catch 块中处理错误消息的显示。


                            // 检查游戏是否已胜利 (model.isWon() 在 tick 中更新)
                            if (model.isWon()) {
                                // 游戏胜利的消息已经在上面处理了 (无论是通过 result.getMessage() 还是特殊处理)。
                                // 下一轮循环时，isWon() 为 true，将显示“Game won. Enter 'new game' or 'quit'”提示。
                            }

                        } catch (InvalidWordException e) {
                            // **捕获 tick 中抛出的 InvalidWordException (例如不在字典中，长度错误，一个字母差异不符)**
                            displayGameState(model); // 显示玩家输入之前的游戏状态 (因为无效输入未添加到路径)
                            // 根据 Model 的 showErrorsFlag 决定是否显示详细错误信息
                            if (model.isShowErrorsFlag()) {
                                System.out.println("Error: " + e.getMessage()); // 显示异常中的错误消息
                            } else {
                                // 如果 showErrorsFlag 是 false，不显示详细错误，只提示输入无效
                                System.out.println("Invalid input.");
                            }
                        } catch (RuntimeException e) {
                            // **捕获 tick 中抛生的其他意外运行时错误**
                            displayGameState(model); // 显示发生错误之前的游戏状态
                            System.err.println("An unexpected error occurred during tick: " + e.getMessage());
                            e.printStackTrace(); // 打印堆栈信息帮助调试
                        }
                    } else {
                        // **输入不是命令，也不是符合长度的词**
                        displayGameState(model); // 显示当前游戏状态
                        // 提示用户输入无效，并告知正确的输入格式或命令
                        System.out.println("Invalid input: Please enter a " + (model.getTargetWord() != null ? model.getTargetWord().length() : "correct") + "-letter word or a valid command.");
                    }
                }
            } else {
                // **如果游戏已胜利，并且用户输入的不是 'quit', 'reset', 'new game' 命令**
                // 将其视为无效输入
                displayInvalidCommand(inputLine);
                // 已经在循环开始时提示用户输入 'new game' 或 'quit' 了。
            }
        }
        // 循环在这里结束，因为 gameIsRunning 变为 false ('quit' 命令)
    }

    /**
     * Displays the current game state to the console.
     * Includes Start Word, Target Word, and the current path with validation results.
     *
     * @param model The WeaverModel instance.
     */
    private static void displayGameState(WeaverModel model) {
        System.out.println("\n--- Current Game State ---");
        // 通过 Model 的公共 getter 方法获取游戏状态信息
        System.out.println("Start Word: " + model.getInitialWord()); // 假设 model.getInitialWord() 存在
        System.out.println("Target Word: " + model.getTargetWord()); // 假设 model.getTargetWord() 存在
        System.out.println("Path:");

        ArrayList<String> path = model.getCurrentPath(); // 假设 model.getCurrentPath() 存在并返回副本
        ArrayList<ValidationResult> results = model.getResultsPath(); // 假设 model.getResultsPath() 存在并返回副本

        // 显示路径
        if (path != null && !path.isEmpty()) {
            // 显示起始词
            System.out.println("  " + path.get(0));

            // 显示后续词语和验证结果
            for (int i = 1; i < path.size(); i++) {
                String word = path.get(i);
                // 获取对应的验证结果，注意索引 (results 列表比 path 列表少 1)
                ValidationResult result = (results != null && results.size() > i - 1) ? results.get(i - 1) : null;
                // 使用 helper 方法 formatValidationResult 格式化验证结果的显示
                System.out.println("  " + word + " " + formatValidationResult(result));
            }
        } else {
            // 路径为空的情况 (理论上 initialize 后不应该为空)
            System.out.println("  (Path is empty)");
        }
        System.out.println("--------------------------");

        // 胜利消息在 runGameLoop 中 tick 之后检查并根据 showErrorsFlag 显示。
        // 这里不再重复显示胜利消息。
    }

    /**
     * Formats a ValidationResult for CLI display.
     * Shows the letter states using simple characters (G, Y, L, ?).
     * Assumes 4-letter words.
     *
     * @param result The validation result. Can be null.
     * @return A string representation of the result (e.g., "[G Y G L]"). Returns "" if result is null or invalid.
     */
    private static String formatValidationResult(ValidationResult result) {
        if (result == null || result.getLetterStates() == null) {
            return ""; // 如果结果为 null 或没有字母状态，返回空字符串
        }
        StringBuilder sb = new StringBuilder("["); // 开始构建字符串，例如 "[G Y L ?]"
        Map<Integer, LetterState> states = result.getLetterStates(); // 获取字母状态 Map

        // 假设单词长度是固定的 (例如 4)，遍历索引 0 到 3
        for (int i = 0; i < 4; i++) { // 遍历字母的索引 (0, 1, 2, 3)
            LetterState state = states.get(i); // 获取当前索引的字母状态
            if (state != null) {
                // 根据 LetterState 枚举值附加对应的字符
                switch (state) {
                    case CORRECT_POSITION: sb.append("G"); break; // 绿色
                    case WRONG_POSITION: sb.append("Y"); break; // 黄色
                    case NOT_IN_WORD: sb.append("L"); break; // 浅灰色 (或灰色)
                    default: sb.append("?"); break; // 未知的状态
                }
            } else {
                sb.append("?"); // 如果 Map 中缺少这个索引的状态，显示问号
            }
            // 在除了最后一个字母的状态后面添加空格
            if (i < 3) { // 索引 0, 1, 2 后面添加空格
                sb.append(" ");
            }
        }
        sb.append("]"); // 结束构建字符串
        return sb.toString(); // 返回格式化后的结果字符串
    }


    /**
     * Displays the full solution path in the CLI.
     * Retrieves the path from the model and formats it using validation results.
     *
     * @param model The WeaverModel instance.
     */
    private static void displaySolutionPath(WeaverModel model) {
        System.out.println("\n--- Full Solution Path ---");
        // 从 Model 获取完整路径 (假设 model.getFullSolutionPath() 存在并返回副本)
        ArrayList<String> fullPath = model.getFullSolutionPath();
        // 从 Model 获取目标词 (假设 model.getTargetWord() 存在)
        String targetWord = model.getTargetWord();
        // 从 Model 获取字典 (假设 model.getDictionary() 存在并返回副本)
        ArrayList<String> dictionary = model.getDictionary();

        if (fullPath == null || fullPath.isEmpty()) {
            System.out.println("No path available."); // 如果路径为空或 null
        } else {
            // 获取路径中每一步的验证结果，用于显示颜色
            // 使用 PathFinder.getValidations 方法
            // 假设 PathFinder.getValidations(目标词, 路径, 字典) 的签名和功能正确 (内部使用 BasicValidator)
            ArrayList<ValidationResult> results = PathFinder.getValidations(targetWord, fullPath, dictionary);

            // 显示路径的第一个词 (起始词)
            if (!fullPath.isEmpty()) {
                System.out.println("  " + fullPath.get(0));
            }

            // 显示路径中的后续词语和验证结果
            for (int i = 1; i < fullPath.size(); i++) {
                String word = fullPath.get(i);
                // 获取对应的验证结果，注意索引 (results 列表比 fullPath 列表少 1)
                ValidationResult result = (results != null && results.size() > i - 1) ? results.get(i - 1) : null;
                // 使用 helper 方法 formatValidationResult 格式化验证结果的显示
                System.out.println("  " + word + " " + formatValidationResult(result));
            }
        }
        System.out.println("--------------------------"); // 结束路径显示
    }


    /**
     * Displays a prompt message to the user for input.
     */
    private static void promptForInput() {
        System.out.print("Enter your next word or command ('quit', 'reset', 'new game', 'show path', 'set errors [on|off]', 'set random [on|off]'): ");
    }

    /**
     * Displays a message indicating that the user's input was invalid.
     *
     * @param input The invalid input string entered by the user.
     */
    private static void displayInvalidCommand(String input) {
        System.out.println("Invalid input or command: '" + input + "'.");
    }
}