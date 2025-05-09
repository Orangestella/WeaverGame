// File: WeaverModel.java
// Based on code_2.txt

import exceptions.InvalidWordException;
import exceptions.WordGenerationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Observable;
import java.util.Set;
import java.util.HashSet;

// 确保这些类在正确的包中或已正确导入
// import your_package_name.GameState;
// import your_package_name.ValidationResult;
// import your_package_name.LetterState;
// import your_package_name.Notification;
// import your_package_name.PathFinder; // 需要导入 PathFinder
// import your_package_name.StrategyFactory;
// import your_package_name.FixedStrategyFactory;
// import your_package_name.RandomStrategyFactory;
// import your_package_name.WordGenerationStrategy;
// import your_package_name.WithPath; // Keep WithPath if needed by strategy
// import your_package_name.WordValidator;
// import your_package_name.BasicValidator; // 需要导入 BasicValidator
// import your_package_name.WithWarning;

public class WeaverModel extends Observable {
    private ArrayList<String> dictionary;
    private ArrayList<String> currentPath;
    private ArrayList<ValidationResult> resultsPath;
    private String initialWord;
    private String targetWord;
    private StrategyFactory strategyFactory;
    private WordGenerationStrategy wordGenerationStrategy;
    private boolean isWon;

    private boolean showErrorsFlag = false;
    // 移除: private boolean showPathFlag = false; // 不再需要这个标志来控制主界面显示

    private boolean randomWordFlag = false;

    private WordValidator validator;
    private WordValidator baseValidator; // 添加基础 validator 字段 (不带装饰器)

    /**
     * Constructs a new WeaverModel.
     * Loads the dictionary and initializes validator and strategy based on default flags.
     *
     * @throws IOException if the dictionary file cannot be loaded.
     */
    public WeaverModel() throws IOException {
        loadDictionary();
        this.baseValidator = new BasicValidator(); // 初始化基础 Validator
        updateValidator(); // 根据默认标志设置初始的 Validator (可能带 WithWarning)
        updateStrategy(); // 根据默认标志设置初始的 Strategy (可能带 WithPath)
        // 游戏将在后续由 Controller 在 GUIMain 或 CLIMain 中调用 initialize 方法进行初始化。
    }

    /**
     * Loads the dictionary words from the dictionary.txt file.
     * Only loads 4-letter words and converts them to uppercase.
     *
     * @throws IOException if the dictionary file cannot be read or is not found.
     */
    private void loadDictionary() throws IOException {
        dictionary = new ArrayList<>();
        // 使用 getResourceAsStream 从 classpath 中加载字典文件
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
        // 确保字典中有足够的词 (至少 2 个)
        if (dictionary == null || dictionary.size() < 2) {
            throw new IOException("Dictionary does not contain enough 4-letter words (requires at least 2).");
        }
    }

    /**
     * Initializes or resets the game.
     * Selects initial and target words based on the randomWordFlag,
     * clears the current path and results, and notifies observers.
     */
    public void initialize() {
        // 根据当前的标志更新 Strategy 和 Validator
        updateStrategy(); // 可能根据 randomWordFlag 选择 WithPath
        updateValidator(); // 可能根据 showErrorsFlag 添加 WithWarning

        // 使用当前 Strategy 生成起始词和目标词
        String[] words;
        try {
            // generateWords 方法可能由 WithPath 装饰器包装，以确保生成的词对之间有路径
            words = wordGenerationStrategy.generateWords(dictionary);
        } catch (WordGenerationException e) {
            // 处理 Strategy 无法生成词对的情况 (例如，WithPath 找不到路径)
            String runtimeWarning = "Could not generate initial/target words: " + e.getMessage();
            System.err.println(runtimeWarning);
            // 在出错时设置一个错误状态并通知 View
            if (this.initialWord == null || this.targetWord == null) {
                // 如果是第一次初始化就失败
                this.initialWord = "####"; // 表示错误状态的词
                this.targetWord = "####";
                this.isWon = false;
                currentPath = new ArrayList<>();
                currentPath.add(initialWord); // 即使出错也添加起始词
                resultsPath = new ArrayList<>();
                notifyUpdate("Initialization failed.", runtimeWarning);
                return; // 停止初始化过程
            } else {
                // 如果重新初始化失败，保留上一次的词对，并通知 View
                notifyUpdate("Could not generate new words. Keeping previous.", runtimeWarning);
                // 清空当前路径和结果，但保留旧的 initialWord 和 targetWord
                this.isWon = false;
                currentPath = new ArrayList<>();
                currentPath.add(initialWord);
                resultsPath = new ArrayList<>();
                return; // 停止初始化过程
            }
        }

        // 如果成功生成词对
        this.initialWord = words[0];
        this.targetWord = words[1];

        this.isWon = false;
        currentPath = new ArrayList<>(); // 重新初始化玩家路径
        currentPath.add(initialWord); // 将起始词添加到玩家路径中
        resultsPath = new ArrayList<>(); // 重新初始化验证结果列表

        // 通知 View 关于初始游戏状态
        // notifyUpdate 方法将根据 showErrorsFlag 决定是否显示“游戏开始”的提示
        notifyUpdate("Game started. Enter your first word.", null); // 发送初始化消息
    }

    /**
     * Processes a player's word input.
     * Validates the word, updates the game state (player's path and results),
     * and notifies observers.
     *
     * @param word The word entered by the player.
     */
    public void tick(String word) {
        // 在处理玩家输入前，确保 Strategy 和 Validator 是最新的 (根据标志)
        updateStrategy(); // 可能根据 randomWordFlag 更新 Strategy (With Path)
        updateValidator(); // 可能根据 showErrorsFlag 更新 Validator (With Warning)

        word = word.toUpperCase(); // 将玩家输入转换为大写字母
        String message = null; // 用于存储主要的提示信息
        String runtimeWarning = null; // 用于存储运行时错误信息

        // 使用临时的列表来存储可能的下一个路径和结果，只有验证成功后才更新正式的 currentPath 和 resultsPath
        ArrayList<String> nextPath = new ArrayList<>(currentPath);
        ArrayList<ValidationResult> nextResultsPath = new ArrayList<>(resultsPath);


        try {
            // **第一步：基本验证 (字典中是否存在，长度是否正确)**
            // validator 字段已经根据 showErrorsFlag 自动包含了 WithWarning 装饰器 (如果需要的话)
            // 确保 validator.validate(inputWord, targetWord, dictionary) 的参数顺序正确
            ValidationResult result = validator.validate(word, this.targetWord, this.dictionary);

            // **第二步：游戏规则验证 (与上一个词是否只差一个字母)**
            // 这个检查应该在基本验证之后，并在将词添加到路径之前
            if (!nextPath.isEmpty()) { // 确保玩家路径不为空 (至少包含起始词)
                String lastWord = nextPath.get(nextPath.size() - 1); // 获取路径中的上一个词
                // isOneLetterDifferent 是一个私有助手方法，检查两个词是否只差一个字母
                if (!isOneLetterDifferent(lastWord, word)) {
                    // 如果不符合一个字母差异规则，抛出 InvalidWordException
                    throw new InvalidWordException("Word must differ by exactly one letter from the previous word.");
                }
            } else {
                // 理论上，如果 initialize 方法正确地将起始词添加到 currentPath，这个 else 分支不应该被玩家输入触发。
                // 如果 tick 在 path 为空时被调用，通常意味着游戏流程有问题。
                // 为了稳妥，可以在这里抛出异常或采取其他错误处理措施。
                throw new InvalidWordException("Game state error: Path is empty before the first player input step.");
            }

            // **如果所有验证都通过 (基本验证 + 一个字母差异规则)，则将词和结果添加到临时的路径列表中**
            nextPath.add(word); // 将通过验证的玩家输入词添加到临时路径
            nextResultsPath.add(result); // 将验证结果添加到临时结果列表

            // **更新正式的 currentPath 和 resultsPath**
            this.currentPath = nextPath;
            this.resultsPath = nextResultsPath;

            // **检查是否胜利**
            this.isWon = result.getValid(); // 胜利条件是最后一个词的验证结果有效 (与目标词一致)

            // **设置主要的提示信息**
            // 如果 WithWarning 装饰器活跃，result.getMessage() 会包含错误或“You win!”/“Valid word.”等信息
            // 如果 WithWarning 不活跃，result.getMessage() 可能是 null 或默认值
            message = result.getMessage();

            // 如果游戏胜利，确保发送一个明确的胜利消息 (即使 WithWarning 已经设置了)
            if (this.isWon) {
                message = "You won the game!"; // 覆盖可能的 WithWarning 消息，确保胜利消息一致
            }

        } catch (InvalidWordException e) {
            // **如果捕获到 InvalidWordException (来自 Validator 或一个字母差异检查)**
            // 设置错误消息，玩家路径和结果列表不会更新
            message = e.getMessage(); // 获取异常中的错误消息
            this.isWon = false; // 确保游戏未胜利状态
        } catch (RuntimeException e) {
            // **捕获处理过程中可能发生的其他意外运行时错误**
            runtimeWarning = "An unexpected error occurred while processing your input: " + e.getMessage();
            System.err.println(runtimeWarning); // 同时打印到控制台方便调试
            this.isWon = false; // 确保游戏未胜利状态
        }

        // **通知 View 更新**
        // 调用内部的 notifyUpdate 方法，它将根据 showErrorsFlag 决定是否发送消息
        notifyUpdate(message, runtimeWarning);

        // 如果游戏胜利，可能需要 Controller 或 View 禁用进一步的输入
        // 这通常在 View 接收到 GameState 并检查 isWon 状态后处理。
    }

    /**
     * Helper method to check if two words of the same length differ by exactly one letter.
     * Assumes words are of the same length (Validator should enforce this).
     * @param word1 The first word.
     * @param word2 The second word.
     * @return True if they differ by exactly one letter, false otherwise.
     */
    private boolean isOneLetterDifferent(String word1, String word2) {
        // 额外的长度检查，尽管 Validator 也应该检查
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
     * This version always provides the player's current progress state in the GameState object.
     *
     * @param hint The primary message (e.g., from Validator or Exception) to potentially display. Can be null.
     * @param runtimeWarning A warning message (e.g., from unexpected errors) to potentially display. Can be null.
     */
    private void notifyUpdate(String hint, String runtimeWarning) {
        setChanged(); // 标记 Model 已改变

        // 创建包含当前游戏状态的 GameState 对象
        GameState currentState = new GameState(this.initialWord, this.targetWord, this.currentPath, this.resultsPath, this.isWon);

        String messageToSend = null; // 最终发送给 View 的 hint
        String warningToSend = null; // 最终发送给 View 的 runtimeWarning

        // **根据 showErrorsFlag 决定是否发送实际的提示信息**
        if (this.showErrorsFlag) {
            // 如果 showErrorsFlag 为 true，发送传入的 hint 和 runtimeWarning

            warningToSend = runtimeWarning; // 运行时警告始终在标志为 true 时发送

            // 确定主要的提示信息 (hint)
            // 如果传入的 hint 不为空，优先使用它
            if (hint != null && !hint.isEmpty()) {
                messageToSend = hint; // 使用传入的 hint (来自 Validator 或 Exception)
            } else {
                // 如果没有具体的 hint，根据游戏状态设置一个默认提示
                if (this.isWon) {
                    messageToSend = "You won the game!"; // 游戏胜利时的默认提示
                } else {
                    messageToSend = "Continue playing."; // 游戏进行中的默认提示
                }
            }
        } else {
            // 如果 showErrorsFlag 为 false，将发送给 View 的提示信息设置为空，以隐藏它们
            messageToSend = null; // 或设置为 ""
            warningToSend = null; // 或设置为 ""
        }

        // 创建 Notification 对象，包含 GameState 和经过处理的提示信息
        // Notification 类不再需要包含 showErrorsFlag 字段，因为 Model 已经在这里处理了消息的可见性
        // 确保 Notification 构造函数是 Notification(GameState gameState, String hint, String runtimeWarning)
        Notification notification = new Notification(currentState, messageToSend, warningToSend);

        // 通知所有注册的观察者 (GUIView)
        notifyObservers(notification);
    }


    /**
     * Triggers a notification to observers with the current game state.
     * This method is called by the Controller when the view needs to be updated
     * based on flag changes or initial display, without player input.
     */
    public void notifyObserversWithCurrentState() {
        // 调用内部的 notifyUpdate 方法，它将根据 showErrorsFlag 决定是否发送消息
        // 传入 null 作为初始 hint 和 runtimeWarning，让 notifyUpdate 根据状态生成默认消息 (如果 flag 允许)
        notifyUpdate(null, null);
    }


    // --- Getters and Setters for Flags ---

    public boolean isShowErrorsFlag() {
        return showErrorsFlag;
    }

    /**
     * Sets the show errors flag and updates the validator.
     *
     * @param showErrorsFlag The new value for the flag.
     */
    public void setShowErrorsFlag(boolean showErrorsFlag) {
        // 只有当标志的值发生改变时才执行更新
        // 您之前发现的初始值错误在这里修正： private boolean showErrorsFlag = false;
        if (this.showErrorsFlag != showErrorsFlag) {
            this.showErrorsFlag = showErrorsFlag; // 更新 showErrorsFlag 的状态
            updateValidator(); // 根据新的 showErrorsFlag 状态更新 Validator (添加或移除 WithWarning)
            // 触发 View 更新，以便 View 根据新的 showErrorsFlag 状态决定是否显示消息
            notifyObserversWithCurrentState();
        }
    }

    // **移除与 showPathFlag 相关的 getter 和 setter 方法**
    // public boolean isShowPathFlag() { ... } // 移除
    // public void setShowPathFlag(boolean showPathFlag) { ... } // 移除

    public boolean isRandomWordFlag() {
        return randomWordFlag;
    }

    /**
     * Sets the random word flag and updates the strategy.
     *
     * @param randomWordFlag The new value for the flag.
     */
    public void setRandomWordFlag(boolean randomWordFlag) {
        if (this.randomWordFlag != randomWordFlag) { // 只有当标志的值发生改变时才执行更新
            this.randomWordFlag = randomWordFlag; // 更新 randomWordFlag 的状态
            updateStrategy(); // 根据新的 randomWordFlag 状态更新 Strategy (可能选择 WithPath)
            // 注意：修改 randomWordFlag 通常需要开始一个新游戏 (调用 initialize) 才会对起始词和目标词生效。
            // 这里只需要更新 Strategy，不需要立即通知 View，除非您希望 View 显示“标志已改变，开始新游戏”之类的消息。
        }
    }

    // --- Internal Update Methods ---

    /**
     * Updates the word generation strategy based on current flags (randomWordFlag).
     * Decides whether to use a basic strategy or wrap it with WithPath.
     */
    public void updateStrategy() {
        // 根据 randomWordFlag 选择 StrategyFactory (Fixed 或 Random)
        StrategyFactory factory;
        if (randomWordFlag) {
            factory = new RandomStrategyFactory(); // 随机词生成器
        } else {
            // 固定词生成器
            // 确保字典已加载且有足够的词
            if (dictionary == null || dictionary.size() < 2) {
                System.err.println("Error: Dictionary not loaded or insufficient words for fixed strategy.");
                // 如果字典有问题，固定词生成器可能无法工作，这里可以考虑抛出异常或使用一个备用的策略
                // 根据示例，固定词是 PORE 和 RUDE
                factory = new FixedStrategyFactory("PORE", "RUDE"); // 确保 FixedStrategyFactory 构造函数匹配
            } else {
                // 使用固定的起始词和目标词，例如 PORE 和 RUDE
                // 根据您提供的代码 (Source 547)，之前使用的是 dictionary.get(1) 和 dictionary.get(0)，这看起来可能不是您想要的固定词。
                // 如果您希望固定词是 PORE 到 RUDE，请确保 FixedStrategyFactory 使用 "PORE" 和 "RUDE"。
                factory = new FixedStrategyFactory("PORE", "RUDE"); // 例如固定为 PORE 和 RUDE
            }
        }

        // **根据 randomWordFlag 决定是否应用 WithPath 装饰器**
        // WithPath 装饰器用于在生成随机词时确保它们之间有路径。
        // 如果是固定词，通常假定路径存在 (例如 PORE 到 RUDE)。
        // 所以，只在生成随机词时才使用 WithPath。
        WordGenerationStrategy base = factory.createStrategy(dictionary); // 创建基础 Strategy
        this.wordGenerationStrategy = randomWordFlag ? // **只在 randomWordFlag 为 true 时使用 WithPath**
                new WithPath(base): // WithPath 会在其 generateWords 方法中查找路径
                base; // 不使用 WithPath
        // WithPath 会在 initialize() 调用其 generateWords() 时查找路径。
        // 获取路径的方法 now 是 getFullSolutionPath() 调用 PathFinder.findPathByBFS。
    }

    /**
     * Updates the word validator based on the showErrorsFlag.
     * Applies the WithWarning decorator if the flag is true.
     */
    public void updateValidator() {
        // 使用基础 Validator (BasicValidator)
        this.validator = this.baseValidator; // 从基础 Validator 开始

        // **如果 showErrorsFlag 为 true，应用 WithWarning 装饰器**
        if (showErrorsFlag) {
            this.validator = new WithWarning(this.validator); // 将 BasicValidator 包装在 WithWarning 中
        }
    }

    /**
     * Resets the current game state, clearing the player's path and results
     * while keeping the same initial and target words.
     */
    public void resetGame() {
        this.isWon = false; // 重置为未胜利状态
        currentPath = new ArrayList<>(); // 清空玩家路径
        currentPath.add(initialWord); // 添加起始词回路径
        resultsPath = new ArrayList<>(); // 清空验证结果列表

        // 通知 View 关于重置后的游戏状态
        // notifyUpdate 方法将根据 showErrorsFlag 决定是否显示消息
        notifyUpdate("Game reset. Enter your first word.", null); // 发送重置消息
    }

    // --- Getters for Controller ---

    /**
     * Gets the initial word of the current game.
     * @return The initial word.
     */
    public String getInitialWord() {
        return initialWord;
    }

    /**
     * Gets the target word of the current game.
     * @return The target word.
     */
    public String getTargetWord() {
        return targetWord;
    }

    /**
     * Gets the dictionary.
     * @return The dictionary.
     */
    public ArrayList<String> getDictionary() {
        return dictionary;
    }

    /**
     * Gets the full solution path from the initial word to the target word.
     * This method calculates the path on demand and does NOT affect the game state
     * or notify observers about the main game board.
     * @return The full solution path as an ArrayList of strings, or an empty list if no path is found.
     */
    public ArrayList<String> getFullSolutionPath() {
        // 计算路径，不依赖于 Strategy 是否是 WithPath
        // 始终使用 PathFinder.findPathByBFS 来查找路径
        // 确保 PathFinder.findPathByBFS 存在并接收正确的参数 (起始词, 目标词, 字典)
        ArrayList<String> fullPath = PathFinder.findPathByBFS(this.initialWord, this.targetWord, this.dictionary);

        // PathFinder.findPathByBFS 应该在找不到路径时返回一个空列表。
        return fullPath;
    }
}