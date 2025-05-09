// File: GUIController.java
// Based on code_2.txt

import exceptions.InvalidWordException; // 假设 InvalidWordException 类存在

import java.util.ArrayList;

// 假设 PathSolutionView 类存在并位于 correct_package.PathSolutionView



public class GUIController {

    private WeaverModel model; // WeaverModel 的引用
    private GUIView view;     // GUIView 的引用
    private StringBuilder currentInputWord; // 用于构建玩家当前输入的单词

    // 构造函数
    public GUIController(WeaverModel model, GUIView view) {
        this.model = model;
        this.view = view;
        this.currentInputWord = new StringBuilder(); // 初始化输入缓冲区

        // View 注册为 Model 的观察者
        model.addObserver(view);
        // 在 View 中设置 Controller，以便 View 可以绑定事件监听器到 Controller 的方法
        view.setController(this);
        // 初始化 Model (通常在主应用程序的 main 方法中调用 model.initialize())
        // model.initialize(); // 确保在正确的地方调用 initialize
    }

    /**
     * Handles the action when the "Reset" button is clicked.
     * Resets the current game state while keeping the same words.
     */
    public void handleResetAction() {
        // 调用 Model 的 resetGame 方法， Model 会清空路径和结果并通知 View
        model.resetGame();
        // 清空 Controller 中的输入缓冲区
        currentInputWord.setLength(0);
        // 更新 View 中显示当前输入的标签
        view.updateInputDisplay("");
        // 初始状态下重置按钮是禁用的
        view.setResetButtonEnabled(false);
        // 不再需要请求窗口焦点
        // view.requestFocusInWindow();
    }

    /**
     * Handles the action when the "New Game" button is clicked.
     * This starts a new game with potentially new words.
     */
    public void handleNewGameAction() {
        // 调用 Model 的 initialize 方法开始新游戏，Model 会生成新词并通知 View
        model.initialize();
        // 清空 Controller 中的输入缓冲区
        currentInputWord.setLength(0);
        // 更新 View 中显示当前输入的标签
        view.updateInputDisplay("");
        // 初始状态下重置按钮是禁用的
        view.setResetButtonEnabled(false);
        // 不再需要请求窗口焦点
        // view.requestFocusInWindow();
    }

    /**
     * Handles the state change of the "Show Errors" checkbox.
     * Updates the flag in the model.
     * @param showErrors The new state of the checkbox (true if selected).
     */
    public void handleShowErrorsFlag(boolean showErrors) {
        // 调用 Model 的 setShowErrorsFlag 方法更新标志
        model.setShowErrorsFlag(showErrors);
        // Model 的 setShowErrorsFlag 方法会更新 Validator 并通知 View，View 会根据新的标志决定是否显示消息。
        // 不再需要请求窗口焦点
        // view.requestFocusInWindow();
    }

    /**
     * Handles the state change of the "Random Words" checkbox.
     * Updates the flag in the model.
     * @param randomWords The new state of the checkbox (true if selected).
     */
    public void handleRandomWordFlag(boolean randomWords) {
        // 调用 Model 的 setRandomWordFlag 方法更新标志
        model.setRandomWordFlag(randomWords);
        // Model 的 setRandomWordFlag 方法会更新 Strategy。
        // 注意：修改 randomWordFlag 通常需要开始一个新游戏才会生效。
        // 不再需要请求窗口焦点
        // view.requestFocusInWindow();
    }

    /**
     * Handles a key press event from the virtual keyboard.
     * Determines the action based on the key text and calls the appropriate handler.
     * @param keyText The text of the button pressed (e.g., "A", "ENTER", "DEL").
     */
    public void handleVirtualKeyPress(String keyText) {
        // 将按钮文本转换为大写，以进行统一处理
        String upperKeyText = keyText.toUpperCase();

        // 根据按钮文本处理不同的按键动作
        if ("ENTER".equals(upperKeyText)) {
            processEnterKey(); // 处理 Enter 键点击
        } else if ("<-".equals(upperKeyText) || "DEL".equals(upperKeyText)) { // 处理退格/删除键点击 (支持 "<-" 或 "DEL")
            processBackspaceKey(); // 处理 Backspace/Delete 键点击
        } else if (upperKeyText.length() == 1 && Character.isLetter(upperKeyText.charAt(0))) {
            // 处理字母键点击，确保只处理单个字母
            processLetterKey(upperKeyText.charAt(0)); // 处理字母输入
        }
        // 不再需要请求窗口焦点
        // view.requestFocusInWindow();
    }


    /**
     * Processes a letter key press from the virtual keyboard.
     * Appends the letter to the current input word if the length limit is not reached.
     * @param letter The pressed letter character (should be uppercase).
     */
    private void processLetterKey(char letter) {
        // 确保目标词不为 null 且当前输入长度未超过目标词长度
        if (model.getTargetWord() != null && currentInputWord.length() < model.getTargetWord().length()) {
            currentInputWord.append(letter); // 添加字母到输入缓冲区
            view.updateInputDisplay(currentInputWord.toString()); // 更新 View 中显示当前输入的标签
            // 可选：在消息标签中显示当前输入 (可能会比较频繁)
            // view.setMessage("Current input: " + currentInputWord.toString());
        }
    }

    /**
     * Processes the Enter key press from the virtual keyboard.
     * Submits the current input word to the model if it has the correct length.
     */
    private void processEnterKey() {
        // 确保目标词不为 null
        if (model.getTargetWord() != null) {
            // 检查当前输入长度是否等于目标词长度
            if (currentInputWord.length() == model.getTargetWord().length()) {
                String wordToSubmit = currentInputWord.toString(); // 获取要提交的单词
                // 在提交给 Model 之前，清空输入缓冲区和显示，以避免 Model 处理期间 View 显示旧输入
                currentInputWord.setLength(0);
                view.updateInputDisplay("");
                view.setMessage("Submitting: " + wordToSubmit); // 显示提交消息
                model.tick(wordToSubmit); // **将单词提交给 Model 进行验证和处理**
            } else {
                // 如果输入长度不正确，显示错误提示
                view.setMessage("Word must be " + model.getTargetWord().length() + " letters long.");
            }
        } else {
            // 如果游戏未初始化，显示提示
            view.setMessage("Game not initialized. Start a new game.");
        }
    }

    /**
     * Processes the Backspace/Delete key press from the virtual keyboard.
     * Removes the last character from the current input word.
     */
    private void processBackspaceKey() {
        // 如果输入缓冲区不为空，删除最后一个字符
        if (currentInputWord.length() > 0) {
            currentInputWord.setLength(currentInputWord.length() - 1);
            view.updateInputDisplay(currentInputWord.toString()); // 更新 View 中显示当前输入的标签
            // 可选：更新消息标签 (可能会比较频繁)
            // view.setMessage("Current input: " + currentInputWord.toString());
        }
    }

    // **移除 handleShowPathFlag 方法**
    // public void handleShowPathFlag(boolean showPath) { ... } // 移除


    /**
     * Handles the action when the "Show Solution Path" button is clicked.
     * Retrieves the solution path from the model and displays it in a new window.
     */
    public void handleShowPathAction() {
        // 从 Model 获取完整的解决方案路径
        // 确保 WeaverModel 中有名为 getFullSolutionPath() 的公共方法
        ArrayList<String> solutionPath = model.getFullSolutionPath();

        // 检查是否成功找到路径 (一个有效的路径至少包含起始词和目标词，长度 >= 2)
        if (solutionPath == null || solutionPath.size() < 2) {
            // 如果没有找到路径，在主界面的消息区域显示提示
            view.setMessage("No solution path found for the current words.");
            return; // 没有找到路径，退出方法
        }

        // 从 Model 获取目标词和字典，这些是 PathSolutionView 需要的数据
        // 确保 WeaverModel 中有名为 getTargetWord() 和 getDictionary() 的公共方法
        String targetWord = model.getTargetWord();
        ArrayList<String> dictionary = model.getDictionary();

        // **创建新的 PathSolutionView 窗口实例**
        // 将解决方案路径、目标词和字典传递给新的 View 的构造函数
        // 确保 PathSolutionView 类存在且其构造函数接收这三个参数
        PathSolutionView pathSolutionView = new PathSolutionView(solutionPath, targetWord, dictionary);

        // **显示新的窗口**
        pathSolutionView.setVisible(true);

        // 注意：这里不要改变主游戏的状态 (例如 isWon, currentPath, resultsPath)。
        // 也不需要调用 model.notifyUpdate() 来更新主游戏界面。
        // 新窗口是独立于主游戏的。
    }
}