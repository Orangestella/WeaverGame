// File: WeaverModel.java
// Based on code_2.txt 提供的当前代码

import exceptions.InvalidWordException;
import exceptions.WordGenerationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Observable; // 保留 Observable 以支持 GUI
import java.util.Set;
import java.util.HashSet;
import java.util.Map; // 需要导入 Map，因为 ValidationResult 中使用了 Map

// 确保这些类在正确的包中或已正确导入
// import your_package_name.GameState; // 如果 notifyUpdate 方法使用了 GameState，需要导入
// import your_package_name.ValidationResult; // **需要导入 ValidationResult**
// import your_package_name.LetterState; // 需要导入 LetterState (ValidationResult 中使用)
// import your_package_name.Notification; // 如果 notifyUpdate 方法使用了 Notification，需要导入
// import your_package_name.PathFinder; // **需要导入 PathFinder** (用于 getFullSolutionPath)
// import your_package_name.StrategyFactory;
// import your_package_name.FixedStrategyFactory;
// import your_package_name.RandomStrategyFactory;
// import your_package_name.WordGenerationStrategy;
// import your_package_name.WithPath; // 保留 WithPath 如果 StrategyFactory 需要它
// import your_package_name.WordValidator; // **需要导入 WordValidator**
// import your_package_name.BasicValidator; // **需要导入 BasicValidator**
// import your_package_name.WithWarning; // **需要导入 WithWarning**

public class WeaverModel extends Observable { // 保留继承 Observable 以支持 GUI
    private ArrayList<String> dictionary; // 字典
    private ArrayList<String> currentPath; // 玩家当前的路径
    private ArrayList<ValidationResult> resultsPath; // 玩家路径中每一步的验证结果
    private String initialWord; // 游戏的起始词
    private String targetWord; // 游戏的目标词
    private StrategyFactory strategyFactory; // 策略工厂，用于生成词对策略
    private WordGenerationStrategy wordGenerationStrategy; // 当前的词对生成策略
    private boolean isWon; // 游戏是否胜利的标志

    private boolean showErrorsFlag = false; // 控制是否显示错误信息
    // 根据您提供的当前代码，showPathFlag 字段已经移除了

    private boolean randomWordFlag = false; // 控制是否随机生成词对

    private WordValidator validator; // 当前活跃的 Validator (可能包含 WithWarning)
    private WordValidator baseValidator; // 基础 Validator (不带装饰器)

    /**
     * Constructs a new WeaverModel.
     * Loads the dictionary and initializes validator and strategy based on default flags.
     *
     * @throws IOException if the dictionary file cannot be loaded.
     */
    public WeaverModel() throws IOException {
        loadDictionary(); // 加载字典
        this.baseValidator = new BasicValidator(); // 初始化基础 Validator
        updateValidator(); // 根据默认标志设置初始的 Validator (可能带 WithWarning)
        updateStrategy(); // 根据默认标志设置初始的 Strategy (可能带 WithPath)
        // 游戏将在后续由 Controller (GUI) 或 CLIMain 调用 initialize() 方法启动。
    }

    /**
     * Loads the dictionary words from the dictionary.txt file.
     * Only loads 4-letter words and converts them to uppercase.
     *
     * @throws IOException if the dictionary file cannot be read or is not found.
     */
    private void loadDictionary() throws IOException {
        dictionary = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/dictionary.txt"),
                        "Dictionary file not found in classpath: /dictionary.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() == 4) {
                    dictionary.add(line.toUpperCase());
                }
            }
        }
        if (dictionary == null || dictionary.size() < 2) {
            throw new IOException("Dictionary does not contain enough 4-letter words (requires at least 2).");
        }
    }

    /**
     * Initializes or resets the game with potentially new words.
     * Selects initial and target words based on the randomWordFlag,
     * clears the current path and results. Notifies observers (for GUI).
     *
     * @throws WordGenerationException if the word generation strategy fails to produce a valid word pair.
     */
    public void initialize() throws WordGenerationException { // 保持抛出异常
        // 根据当前的标志更新 Strategy 和 Validator
        updateStrategy(); // 可能根据 randomWordFlag 选择 WithPath
        updateValidator(); // 可能根据 showErrorsFlag 添加 WithWarning

        // 使用当前 Strategy 生成起始词和目标词
        String[] words;
        // generateWords 方法可能由 WithPath 装饰器包装，以确保生成的词对之间有路径。
        // 如果生成失败，会抛出 WordGenerationException。
        words = wordGenerationStrategy.generateWords(dictionary); // 保持调用不变


        // 如果成功生成词对
        this.initialWord = words[0];
        this.targetWord = words[1];

        this.isWon = false; // 重置为未胜利状态
        currentPath = new ArrayList<>(); // 重新初始化玩家路径
        currentPath.add(initialWord); // 将起始词添加到玩家路径中
        resultsPath = new ArrayList<>(); // 重新初始化验证结果列表

        // **通知 View (GUI) 关于初始游戏状态**
        // notifyUpdate 方法会根据 showErrorsFlag 决定是否在 GUI 中显示“游戏开始”的提示
        notifyUpdate("Game started. Enter your first word.", null); // 保持通知调用
    }

    /**
     * Processes a player's word input, updates the game state, and returns the validation result.
     * Notifies observers (for GUI) about the updated game state.
     *
     * @param word The word entered by the player.
     * @return The ValidationResult for the submitted word. The result includes validation states and potentially a message from decorators.
     * @throws InvalidWordException if basic validation (dictionary/length) or the one-letter rule fails *before* a ValidationResult is fully formed.
     * @throws RuntimeException for other unexpected errors during processing.
     */
    // **修改 tick 方法签名，使其返回 ValidationResult**
    public ValidationResult tick(String word) throws InvalidWordException, RuntimeException { // 保持抛出异常
        word = word.toUpperCase();

        ArrayList<String> nextPath = new ArrayList<>(currentPath);
        ArrayList<ValidationResult> nextResultsPath = new ArrayList<>(resultsPath);

        ValidationResult result = null; // 用于存储本次验证的结果

        try {
            // **第一步：基本验证 (字典中是否存在，长度是否正确)**
            // validator 字段已经根据 showErrorsFlag 自动包含了 WithWarning 装饰器 (如果需要的话)。
            // 如果验证失败 (不在字典，长度错误)，validator.validate 方法会抛出 InvalidWordException。
            // 确保 validator.validate(inputWord, targetWord, dictionary) 的参数顺序正确。
            result = validator.validate(word, this.targetWord, this.dictionary); // 保持调用不变

            // **第二步：游戏规则验证 (与上一个词是否只差一个字母)**
            // 这个检查在基本验证通过后进行。
            if (!nextPath.isEmpty()) { // 确保玩家路径不为空 (至少包含起始词)。
                String lastWord = nextPath.get(nextPath.size() - 1); // 获取路径中的上一个词
                // isOneLetterDifferent 是一个私有助手方法，检查两个词是否只差一个字母。
                if (!isOneLetterDifferent(lastWord, word)) {
                    // 如果不符合一个字母差异规则，抛出 InvalidWordException。
                    // 这个异常将在 CLIMain (和 GUI Controller) 中捕获并显示消息。
                    throw new InvalidWordException("Word must differ by exactly one letter from the previous word."); // 保持抛出
                }
            } else {
                // 如果 tick 在 path 为空时被调用，通常意味着游戏流程有问题。
                // 应该始终从 initialize 添加起始词开始。
                throw new InvalidWordException("Game state error: Path is empty before the first player input step."); // 保持抛出
            }

            // **如果所有验证都通过 (基本验证 + 一个字母差异规则)，则将词和结果添加到临时的路径列表中**
            nextPath.add(word); // 将通过验证的玩家输入词添加到临时路径
            nextResultsPath.add(result); // 将验证结果添加到临时结果列表

            // **更新正式的 currentPath 和 resultsPath**
            this.currentPath = nextPath; // 提交更改
            this.resultsPath = nextResultsPath; // 提交更改

            // **检查是否胜利**
            this.isWon = result.getValid(); // 胜利条件是最后一个词的验证结果有效 (与目标词一致)

            // **设置主要的提示信息**
            // 如果 WithWarning 装饰器活跃，result.getMessage() 会包含错误或“You win!”/“Valid word.”等信息。
            // 如果 WithWarning 不活跃，result.getMessage() 可能是 null 或默认值。
            String message = result.getMessage();
            String runtimeWarning = null; // 运行时警告通常来自 catch 块

            // 如果游戏胜利，确保发送一个明确的胜利消息给 GUI (即使 WithWarning 可能已经设置了)
            if (this.isWon && (message == null || message.isEmpty() || !message.equalsIgnoreCase("You won the game!"))) {
                message = "You won the game!"; // 覆盖可能的 WithWarning 消息，确保胜利消息一致
            }

            // **通知 View (GUI) 更新**
            // notifyUpdate 会根据 showErrorsFlag 决定消息在 GUI 中是否可见。
            notifyUpdate(message, runtimeWarning); // 保持通知调用

            // **返回验证结果供 CLI 使用**
            // CLI 将使用这个返回的 ValidationResult 来获取详细验证状态和消息
            return result; // 返回结果

        } catch (InvalidWordException e) {
            // **如果捕获到 InvalidWordException (来自 Validator 或一个字母差异检查)**
            // 不更新路径和结果列表，游戏状态保持未胜利
            this.isWon = false;

            // 获取异常中的错误消息，用于通知 GUI
            String message = e.getMessage();
            String runtimeWarning = null;

            // **通知 View (GUI) 更新，包含错误消息**
            notifyUpdate(message, runtimeWarning); // 保持通知调用，将错误消息发送给 GUI

            // **将异常重新抛出，以便 CLIMain (和 GUI Controller) 可以捕获并显示错误消息**
            throw e; // 保持重新抛出异常
        } catch (RuntimeException e) {
            // **捕获处理过程中可能发生的其他意外运行时错误**
            // 不更新路径和结果列表，游戏状态保持未胜利
            this.isWon = false;

            // 格式化运行时错误信息，用于通知 GUI
            String message = null; // 没有具体的 hint message
            String runtimeWarning = "An unexpected error occurred during tick: " + e.getMessage();
            System.err.println(runtimeWarning); // 同时打印到控制台方便调试

            // **通知 View (GUI) 更新，包含运行时警告**
            notifyUpdate(message, runtimeWarning); // 保持通知调用，将警告发送给 GUI

            // **将异常重新抛出，由 CLIMain (和 GUI Controller) 捕获并处理**
            throw e; // 保持重新抛出异常
        }
    }

    /**
     * Helper method to check if two words of the same length differ by exactly one letter.
     * Assumes words are of the same length (Validator should enforce this).
     * @param word1 The first word.
     * @param word2 The second word.
     * @return True if they differ by exactly one letter, false otherwise.
     */
    private boolean isOneLetterDifferent(String word1, String word2) {
        // 保持不变，作为私有助手方法
        if (word1 == null || word2 == null || word1.length() != word2.length()) {
            return false;
        }
        int diffCount = 0;
        for (int i = 0; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i)) {
                diffCount++;
            }
        }
        return diffCount == 1;
    }


    /**
     * Notifies observers about the current game state.
     * Conditionally sets the hint and runtimeWarning messages based on the showErrorsFlag.
     * This version provides the player's current progress state in the GameState object.
     * This method is primarily for the GUI.
     *
     * @param hint The primary message (e.g., from Validator or Exception) to potentially display. Can be null.
     * @param runtimeWarning A warning message (e.g., from unexpected errors) to potentially display. Can be null.
     */
    private void notifyUpdate(String hint, String runtimeWarning) {
        // 保持不变，用于通知 GUI
        setChanged();

        // 创建 GameState 对象 (假设 GameState 构造函数和字段匹配)
        GameState currentState = new GameState(this.initialWord, this.targetWord, this.currentPath, this.resultsPath, this.isWon);

        String messageToSend = null;
        String warningToSend = null;

        // 根据 showErrorsFlag 决定发送给 GUI 的消息内容
        if (this.showErrorsFlag) {
            warningToSend = runtimeWarning;
            if (hint != null && !hint.isEmpty()) {
                messageToSend = hint;
            } else {
                // 如果没有具体的 hint，根据游戏状态设置一个默认提示 (在 showErrorsFlag 为 true 时)
                if (this.isWon) {
                    messageToSend = "You won the game!";
                } else {
                    messageToSend = "Continue playing.";
                }
            }
        } else {
            // 如果 showErrorsFlag 为 false，将发送给 GUI 的提示信息设置为空，以隐藏它们
            messageToSend = null;
            warningToSend = null;
        }

        // 创建 Notification 对象 (假设 Notification 构造函数是 Notification(GameState gameState, String hint, String runtimeWarning))
        Notification notification = new Notification(currentState, messageToSend, warningToSend);

        // 通知所有注册的观察者 (GUIView)
        notifyObservers(notification);
    }


    /**
     * Triggers a notification to observers with the current game state.
     * This method is called by the Controller (GUI) when the view needs to be updated
     * based on flag changes or initial display, without player input.
     */
    public void notifyObserversWithCurrentState() {
        // 保持不变，用于触发 GUI 更新
        // 调用内部的 notifyUpdate 方法，传入 null 作为初始 hint/warning
        // notifyUpdate 会根据 showErrorsFlag 和当前状态生成默认消息 (如果允许)
        notifyUpdate(null, null);
    }


    // --- Getters and Setters for Flags ---

    public boolean isShowErrorsFlag() {
        // 保持不变，提供公共 getter
        return showErrorsFlag;
    }

    /**
     * Sets the show errors flag and updates the validator.
     * Notifies observers for GUI update.
     *
     * @param showErrorsFlag The new value for the flag.
     */
    public void setShowErrorsFlag(boolean showErrorsFlag) {
        // 保持不变，更新标志，更新 Validator，并通知 GUI
        if (this.showErrorsFlag != showErrorsFlag) {
            this.showErrorsFlag = showErrorsFlag; // 更新 showErrorsFlag 的状态
            updateValidator(); // 根据新的 showErrorsFlag 状态更新 Validator (添加或移除 WithWarning)
            // 触发 View (GUI) 更新，以便 View 根据新的 showErrorsFlag 状态决定是否显示消息
            notifyObserversWithCurrentState(); // 保持通知调用
        }
    }

    // **移除与 showPathFlag 相关的 getter 和 setter 方法**
    // 根据您提供的当前代码，这些方法应该已经移除了

    public boolean isRandomWordFlag() {
        // 保持不变，提供公共 getter
        return randomWordFlag;
    }

    /**
     * Sets the random word flag and updates the strategy.
     *
     * @param randomWordFlag The new value for the flag.
     */
    public void setRandomWordFlag(boolean randomWordFlag) {
        // 保持不变，更新标志和 Strategy
        if (this.randomWordFlag != randomWordFlag) {
            this.randomWordFlag = randomWordFlag; // 更新 randomWordFlag 的状态
            updateStrategy(); // 根据新的 randomWordFlag 状态更新 Strategy (可能选择 WithPath)
            // 注意：修改 randomWordFlag 通常需要开始一个新游戏 (调用 initialize) 才会对起始词和目标词生效。
        }
    }

    // --- Internal Update Methods ---

    /**
     * Updates the word generation strategy based on current flags (randomWordFlag).
     * Decides whether to use a basic strategy or wrap it with WithPath.
     */
    public void updateStrategy() {
        // 保持不变
        StrategyFactory factory;
        if (randomWordFlag) {
            factory = new RandomStrategyFactory(); // 随机词生成器
        } else {
            // 固定词生成器
            if (dictionary == null || dictionary.size() < 2) {
                System.err.println("Error: Dictionary not loaded or insufficient words for fixed strategy.");
                // 假设字典有问题，固定词生成器可能无法工作，这里使用固定值避免崩溃
                factory = new FixedStrategyFactory("PORE", "RUDE"); // 确保 FixedStrategyFactory 构造函数匹配
            } else {
                // 使用固定的起始词和目标词，例如 PORE 和 RUDE
                factory = new FixedStrategyFactory("PORE", "RUDE");
            }
        }

        WordGenerationStrategy base = factory.createStrategy(dictionary); // 创建基础 Strategy
        // **根据 randomWordFlag 决定是否应用 WithPath 装饰器**
        // WithPath 装饰器用于在生成随机词时确保它们之间有路径。
        // 只在生成随机词时才使用 WithPath。
        this.wordGenerationStrategy = randomWordFlag ?
                new WithPath(base): // 如果随机词，则包装 WithPath
                base; // 如果固定词，则不包装 WithPath
    }

    /**
     * Updates the word validator based on the showErrorsFlag.
     * Applies the WithWarning decorator if the flag is true.
     */
    public void updateValidator() {
        // 保持不变
        this.validator = this.baseValidator; // 从基础 Validator 开始
        if (showErrorsFlag) {
            this.validator = new WithWarning(this.validator); // 如果 showErrorsFlag 为 true，包装 WithWarning
        }
    }

    /**
     * Resets the current game state, clearing the player's path and results
     * while keeping the same initial and target words. Notifies observers (for GUI).
     */
    public void resetGame() {
        // 保持不变，重置游戏状态并通知 GUI
        this.isWon = false; // 重置为未胜利状态
        currentPath = new ArrayList<>(); // 清空玩家路径
        currentPath.add(initialWord); // 添加起始词回路径
        resultsPath = new ArrayList<>(); // 清空验证结果列表

        // 通知 View (GUI) 关于重置后的游戏状态
        // notifyUpdate 方法将根据 showErrorsFlag 决定是否显示消息
        notifyUpdate("Game reset. Enter your first word.", null); // 保持通知调用
    }

    // --- **添加公共 Getter 方法，暴露游戏状态供 CLI 获取** ---
    // 这些 getter 方法不会影响 GUI 的 Observer 模式，只是提供一种拉取状态的手段。
    // 返回 ArrayList 时返回副本，防止外部直接修改 Model 的内部状态。

    /**
     * Gets the initial word of the current game.
     * @return The initial word.
     */
    public String getInitialWord() {
        // **添加公共 Getter**
        return initialWord;
    }

    /**
     * Gets the target word of the current game.
     * @return The target word.
     */
    public String getTargetWord() {
        // **添加公共 Getter**
        return targetWord;
    }

    /**
     * Gets the current player's path.
     * @return The current path as a new ArrayList of strings (a read-only copy).
     */
    public ArrayList<String> getCurrentPath() {
        // **添加公共 Getter**
        // 返回一个副本，防止外部直接修改 Model 的内部列表
        return new ArrayList<>(currentPath);
    }

    /**
     * Gets the validation results for the steps in the player's path.
     * @return The validation results as a new ArrayList of ValidationResult (a read-only copy).
     */
    public ArrayList<ValidationResult> getResultsPath() {
        // **添加公共 Getter**
        // 返回一个副本
        return new ArrayList<>(resultsPath);
    }

    /**
     * Checks if the game is currently won.
     * @return True if the game is won, false otherwise.
     */
    public boolean isWon() {
        // **添加公共 Getter**
        return isWon;
    }

    /**
     * Gets the dictionary.
     * @return The dictionary (a read-only copy recommended).
     */
    public ArrayList<String> getDictionary() {
        // **添加公共 Getter**
        // 返回一个副本
        return new ArrayList<>(dictionary);
    }

    /**
     * Gets the full solution path from the initial word to the target word.
     * This method calculates the path on demand and does NOT affect the game state.
     * @return The full solution path as an ArrayList of strings, or an empty list if no path is found.
     */
    public ArrayList<String> getFullSolutionPath() {
        // 保持不变 (假设 PathFinder.findPathByBFS 存在并接收 correct 参数)
        ArrayList<String> fullPath = PathFinder.findPathByBFS(this.initialWord, this.targetWord, this.dictionary);
        return fullPath; // 返回一个新列表
    }

    // **移除 private notifyFullPathUpdate() 方法** (根据您提供的当前代码，这个方法应该已经移除了)

}