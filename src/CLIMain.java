//// File: CLIMain.java
//
//import exceptions.WordGenerationException;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Scanner;
//import java.util.Map; // 用于处理 ValidationResult 中的 LetterState
//
//// **导入您项目中的其他类**
//// 请根据您的实际包结构，导入 WeaverModel, ValidationResult, LetterState, PathFinder 类
//// import your_package_name.WeaverModel;
//// import your_package_name.ValidationResult;
//// import your_package_name.LetterState;
//// import your_package_name.PathFinder; // 需要 PathFinder 来格式化 show path 的输出
//
//
//public class CLIMain {
//
//    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in); // 用于读取用户输入
//        WeaverModel model = null; // WeaverModel 实例
//
//        try {
//            // 1. 创建 Model
//            // 假设 WeaverModel 的构造函数会抛出 IOException (例如加载字典失败)
//            model = new WeaverModel();
//
//            // 2. 初始化第一局游戏
//            // initializeGame 方法会处理 Model 的初始化和显示初始状态
//            initializeGame(model);
//
//            // 3. 运行主游戏循环
//            runGameLoop(model, scanner);
//
//        } catch (IOException e) {
//            // 捕获 Model 构造函数中的字典加载失败异常
//            System.err.println("Error loading dictionary: " + e.getMessage());
//            // e.printStackTrace(); // 调试时可以打印堆栈
//        } catch (Exception e) {
//            // 捕获游戏设置或主循环中可能发生的其他意外异常
//            System.err.println("An unexpected error occurred: " + e.getMessage());
//            e.printStackTrace(); // 打印堆栈以帮助调试
//        } finally {
//            // 确保 Scanner 在程序结束时被关闭
//            if (scanner != null) {
//                scanner.close();
//            }
//        }
//    }
//
//    /**
//     * Initializes a new game by calling model.initialize() and displays the initial state.
//     * Handles potential WordGenerationException during initialization.
//     *
//     * @param model The WeaverModel instance.
//     * @throws WordGenerationException if word generation fails.
//     */
//    private static void initializeGame(WeaverModel model) throws WordGenerationException {
//        try {
//            model.initialize(); // 调用 Model 的 initialize 方法
//            // 初始化成功后，显示游戏的初始状态
//            displayGameState(model);
//            // 显示游戏开始的提示信息 (这里不受 showErrorsFlag 控制，因为它是一个重要的起始信息)
//            System.out.println("Game started. Enter your first word.");
//        } catch (WordGenerationException e) {
//            // 如果词对生成失败，打印错误信息
//            System.err.println("Failed to initialize game: " + e.getMessage());
//            // 在这个简单的 CLI 中，词对生成失败是一个严重问题，通常会阻止游戏进行。
//            // 选择重新抛出异常，由 main 方法捕获和处理 (例如退出)。
//            throw e;
//        }
//    }
//
//    /**
//     * Runs the main game loop for the command line interface.
//     * Reads user input and processes commands or word guesses.
//     * Continues until the user types 'quit'.
//     *
//     * @param model The WeaverModel instance.
//     * @param scanner The Scanner for reading user input.
//     */
//    private static void runGameLoop(WeaverModel model, Scanner scanner) {
//        boolean gameIsRunning = true; // 控制游戏主循环是否运行
//        while (gameIsRunning) {
//            // 只有在游戏未胜利时才提示用户输入
//            // 如果游戏已胜利，用户必须输入命令 (如 'new game' 或 'quit')
//            if (!model.isWon()) { // 假设 model.isWon() getter 存在
//                promptForInput(); // 显示输入提示
//            } else {
//                // 如果游戏已胜利，不显示常规输入提示，可以提示用户输入命令
//                System.out.print("Game won. Enter 'new game' to play again or 'quit' to exit: ");
//            }
//
//
//            String inputLine = scanner.nextLine().trim(); // 读取用户输入并去除首尾空格
//
//            // **处理退出命令**
//            if (inputLine.equalsIgnoreCase("quit")) {
//                gameIsRunning = false; // 设置循环标志为 false，退出循环
//                System.out.println("Quitting game. Goodbye!");
//                continue; // 结束当前循环迭代，跳到下一次循环条件检查
//            }
//
//            // **处理其他命令或玩家猜词 (只在游戏未胜利时处理猜词)**
//            if (!model.isWon()) {
//                if (inputLine.equalsIgnoreCase("reset")) {
//                    // **处理重置命令**
//                    model.resetGame(); // 调用 Model 的 resetGame 方法
//                    displayGameState(model); // 显示重置后的游戏状态
//                    // Model 的 resetGame 方法现在不发送消息，CLIMain 负责显示重置后的起始提示
//                    System.out.println("Game reset. Enter your first word.");
//                } else if (inputLine.equalsIgnoreCase("new game")) {
//                    // **处理开始新游戏命令**
//                    try {
//                        initializeGame(model); // 调用 initializeGame 方法 (它会调用 model.initialize 并显示初始状态)
//                    } catch (WordGenerationException e) {
//                        // 如果新游戏初始化失败，initializeGame 已经打印了错误信息。
//                        System.err.println("Failed to start a new game.");
//                        // 可以在这里选择退出或让用户再试一次。
//                        // 继续循环让用户有机会输入其他命令或再试一次 'new game'。
//                    }
//                } else if (inputLine.equalsIgnoreCase("show path")) {
//                    // **处理显示路径命令**
//                    displaySolutionPath(model); // 调用 displaySolutionPath 方法显示完整路径
//                } else if (inputLine.toLowerCase().startsWith("set errors ")) {
//                    // **处理设置错误显示命令 (例如 "set errors on", "set errors off")**
//                    String[] parts = inputLine.split("\\s+"); // 按一个或多个空格分割命令
//                    if (parts.length == 3 && parts[1].equalsIgnoreCase("errors")) {
//                        if (parts[2].equalsIgnoreCase("on")) {
//                            model.setShowErrorsFlag(true); // 调用 Model 的 setShowErrorsFlag 方法
//                            System.out.println("Show errors enabled."); // 立即反馈给用户
//                        } else if (parts[2].equalsIgnoreCase("off")) {
//                            model.setShowErrorsFlag(false); // 调用 Model 的 setShowErrorsFlag 方法
//                            System.out.println("Show errors disabled."); // 立即反馈给用户
//                        } else {
//                            displayInvalidCommand(inputLine); // 命令格式错误
//                        }
//                    } else {
//                        displayInvalidCommand(inputLine); // 命令格式错误
//                    }
//                    // Model 的 setShowErrorsFlag 方法会更新 Validator，这会影响后续 tick 的行为。
//                    // 这里不需要更新游戏状态显示，因为标志变化本身不改变路径或结果。
//                    // 反馈信息已经直接打印。
//                }
//                // TODO: 添加其他命令的处理逻辑 (例如 set random on/off)
//                // else if (inputLine.toLowerCase().startsWith("set random ")) { ... }
//
//                else {
//                    // **如果不是识别的命令，假定是玩家猜词**
//                    // 简单的长度检查，防止提交明显错误的输入
//                    if (model.getTargetWord() != null && inputLine.length() == model.getTargetWord().length()) {
//                        try {
//                            // 调用 Model 的 tick 方法处理玩家猜词，并获取返回的 ValidationResult
//                            // Model.tick 方法会抛出 InvalidWordException 或 RuntimeException
//                            ValidationResult result = model.tick(inputLine); // **调用 Model 的 tick 方法**
//
//                            // 如果 tick 成功 (没有抛出异常)，表示输入是一个有效的游戏步骤词
//                            // 显示更新后的游戏状态 (包含新添加的词和结果)
//                            displayGameState(model);
//
//                            // 根据 Model 的 showErrorsFlag 和 ValidationResult 的消息来显示提示信息
//                            // ValidationResult.getMessage() 包含来自 WithWarning 的消息 (如果 showErrorsFlag 为 true 且 WithWarning 活跃的话)
//                            if (model.isShowErrorsFlag() && result.getMessage() != null && !result.getMessage().isEmpty()) {
//                                System.out.println("Message: " + result.getMessage());
//                            }
//
//                            // 检查游戏是否已胜利 (tick 方法会更新 model.isWon())
//                            if (model.isWon()) {
//                                // 如果胜利，已经在 displayGameState 后打印了胜利消息。
//                                // 下一轮循环时，isWon() 为 true，将显示“Game won. Enter 'new game' or 'quit'”提示。
//                            }
//
//                        } catch (InvalidWordException e) {
//                            // **捕获 tick 中抛出的 InvalidWordException (例如不在字典中，长度错误，一个字母差异不符)**
//                            displayGameState(model); // 显示玩家输入之前的游戏状态 (因为无效输入未添加到路径)
//                            // 根据 Model 的 showErrorsFlag 决定是否显示详细错误信息
//                            if (model.isShowErrorsFlag()) {
//                                System.out.println("Error: " + e.getMessage()); // 显示异常中的错误消息
//                            } else {
//                                // 如果 showErrorsFlag 是 false，不显示详细错误，可以只提示输入无效
//                                // 或者不显示任何消息，直接等待下一次输入（取决于设计）
//                                System.out.println("Invalid input."); // 显示一个通用错误提示
//                            }
//                        } catch (RuntimeException e) {
//                            // **捕获 tick 中抛生的其他意外运行时错误**
//                            displayGameState(model); // 显示发生错误之前的游戏状态
//                            System.err.println("An unexpected error occurred during tick: " + e.getMessage());
//                            e.printStackTrace(); // 打印堆栈信息帮助调试
//                        }
//                    } else {
//                        // **输入不是命令，也不是符合长度的词**
//                        // 显示当前游戏状态 (可选，帮助用户了解当前情况)
//                        displayGameState(model);
//                        // 提示用户输入无效，并告知正确的输入格式或命令
//                        System.out.println("Invalid input: Please enter a " + (model.getTargetWord() != null ? model.getTargetWord().length() : "correct") + "-letter word or a valid command.");
//                    }
//                }
//            } else {
//                // **如果游戏已胜利，并且用户输入的不是 'quit', 'reset', 'new game' 命令**
//                // 将其视为无效输入
//                displayInvalidCommand(inputLine);
//                // 已经在循环开始时提示用户输入 'new game' 或 'quit' 了。
//            }
//        }
//        // 循环在这里结束，因为 gameIsRunning 变为 false ('quit' 命令)
//    }
//
//    /**
//     * Displays the current game state to the console.
//     * Includes Start Word, Target Word, and the current path with validation results.
//     *
//     * @param model The WeaverModel instance.
//     */
//    private static void displayGameState(WeaverModel model) {
//        System.out.println("\n--- Current Game State ---");
//        System.out.println("Start Word: " + model.getInitialWord()); // 假设 model.getInitialWord() 存在
//        System.out.println("Target Word: " + model.getTargetWord()); // 假设 model.getTargetWord() 存在
//        System.out.println("Path:");
//
//        ArrayList<String> path = model.getCurrentPath(); // 假设 model.getCurrentPath() 存在并返回副本
//        ArrayList<ValidationResult> results = model.getResultsPath(); // 假设 model.getResultsPath() 存在并返回副本
//
//        // 显示路径
//        if (path != null && !path.isEmpty()) {
//            // 显示起始词
//            System.out.println("  " + path.get(0));
//
//            // 显示后续词语和验证结果
//            for (int i = 1; i < path.size(); i++) {
//                String word = path.get(i);
//                // 获取对应的验证结果，注意索引 (results 比 path 少 1)
//                ValidationResult result = (results != null && results.size() > i - 1) ? results.get(i - 1) : null;
//                // 使用 helper 方法格式化验证结果的显示
//                System.out.println("  " + word + " " + formatValidationResult(result));
//            }
//        } else {
//            // 路径为空的情况 (理论上 initialize 后不应该为空)
//            System.out.println("  (Path is empty)");
//        }
//        System.out.println("--------------------------");
//
//        // 胜利消息在 runGameLoop 中 tick 之后检查并显示。
//        // 这里不再重复显示胜利消息，除非您希望在 reset/new game 后立即显示 (例如如果它们生成了一个已胜利的状态，不常见)。
//    }
//
//    /**
//     * Formats a ValidationResult for CLI display.
//     * Shows the letter states using simple characters (G, Y, L, ?).
//     *
//     * @param result The validation result. Can be null.
//     * @return A string representation of the result (e.g., "[G Y G L]"). Returns "" if result is null or invalid.
//     */
//    private static String formatValidationResult(ValidationResult result) {
//        if (result == null || result.getLetterStates() == null) {
//            return ""; // 如果结果为 null 或没有字母状态，返回空字符串
//        }
//        StringBuilder sb = new StringBuilder("["); // 开始构建字符串，例如 "[G Y L ?]"
//        Map<Integer, LetterState> states = result.getLetterStates(); // 获取字母状态 Map
//
//        // 假设单词长度是固定的 (例如 4)，遍历索引 0 到 3
//        for (int i = 0; i < 4; i++) { // 遍历字母的索引
//            LetterState state = states.get(i); // 获取当前索引的字母状态
//            if (state != null) {
//                // 根据 LetterState 枚举值附加对应的字符
//                switch (state) {
//                    case CORRECT_POSITION: sb.append("G"); break; // 绿色
//                    case WRONG_POSITION: sb.append("Y"); break; // 黄色
//                    case NOT_IN_WORD: sb.append("L"); break; // 浅灰色 (或灰色)
//                    default: sb.append("?"); break; // 未知的状态
//                }
//            } else {
//                sb.append("?"); // 如果 Map 中缺少这个索引的状态，显示问号
//            }
//            // 在除了最后一个字母的状态后面添加空格
//            if (i < 3) { // 索引 0, 1, 2 后面添加空格
//                sb.append(" ");
//            }
//        }
//        sb.append("]"); // 结束构建字符串
//        return sb.toString(); // 返回格式化后的结果字符串
//    }
//
//
//    /**
//     * Displays the full solution path in the CLI.
//     * Retrieves the path from the model and formats it using validation results.
//     *
//     * @param model The WeaverModel instance.
//     */
//    private static void displaySolutionPath(WeaverModel model) {
//        System.out.println("\n--- Full Solution Path ---");
//        // 从 Model 获取完整路径 (假设 model.getFullSolutionPath() 存在并返回副本)
//        ArrayList<String> fullPath = model.getFullSolutionPath();
//        // 从 Model 获取目标词 (假设 model.getTargetWord() 存在)
//        String targetWord = model.getTargetWord();
//        // 从 Model 获取字典 (假设 model.getDictionary() 存在并返回副本)
//        ArrayList<String> dictionary = model.getDictionary();
//
//        if (fullPath == null || fullPath.isEmpty()) {
//            System.out.println("No path available."); // 如果路径为空或 null
//        } else {
//            // 获取路径中每一步的验证结果，用于显示颜色
//            // 使用 PathFinder.getValidations 方法
//            // 假设 PathFinder.getValidations(目标词, 路径, 字典) 的签名和功能正确
//            ArrayList<ValidationResult> results = PathFinder.getValidations(targetWord, fullPath, dictionary);
//
//            // 显示路径的第一个词 (起始词)
//            if (!fullPath.isEmpty()) {
//                System.out.println("  " + fullPath.get(0));
//            }
//
//            // 显示路径中的后续词语和验证结果
//            for (int i = 1; i < fullPath.size(); i++) {
//                String word = fullPath.get(i);
//                // 获取对应的验证结果，注意索引
//                ValidationResult result = (results != null && results.size() > i - 1) ? results.get(i - 1) : null;
//                // 使用 helper 方法格式化验证结果的显示
//                System.out.println("  " + word + " " + formatValidationResult(result));
//            }
//        }
//        System.out.println("--------------------------"); // 结束路径显示
//    }
//
//
//    /**
//     * Displays a prompt message to the user for input.
//     */
//    private static void promptForInput() {
//        System.out.print("Enter your next word or command ('quit', 'reset', 'new game', 'show path', 'set errors [on|off]'): ");
//    }
//
//    /**
//     * Displays a message indicating that the user's input was invalid.
//     *
//     * @param input The invalid input string entered by the user.
//     */
//    private static void displayInvalidCommand(String input) {
//        System.out.println("Invalid input or command: '" + input + "'.");
//    }
//}