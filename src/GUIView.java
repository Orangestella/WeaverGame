// File: GUIView.java
// Based on code_2.txt

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;




public class GUIView extends JFrame implements Observer {

    private static final int WORD_LENGTH = 4; // 单词长度为 4
    private static final int MAX_DISPLAY_ROWS = 10; // 游戏面板最多显示的行数示例

    private JLabel initialWordLabel; // 显示起始词的标签
    private JLabel targetWordLabel;  // 显示目标词的标签
    private JPanel gameBoardPanel;   // 显示单词路径的游戏面板
    private JPanel keyboardPanel;    // 虚拟键盘面板
    private JButton resetButton;     // 重置按钮
    private JButton newGameButton;   // 新游戏按钮
    private JLabel messageLabel;     // 显示提示消息的标签
    private JPanel controlPanel;     // 包含按钮和复选框的控制面板

    private JCheckBox showErrorsCheckBox; // 显示错误复选框
    private JCheckBox randomWordCheckBox; // 随机单词复选框

    // **移除与 showPathCheckBox 相关的代码**
    // private JCheckBox showPathCheckBox;

    // **添加 Show Path 按钮**
    private JButton showPathButton; // 添加显示路径按钮字段

    private GUIController controller; // Controller 的引用

    // **添加用于显示当前玩家输入的 JLabel**
    private JLabel currentInputDisplayLabel; // 显示当前玩家输入的标签


    /**
     * Constructs the GUI view for the Weaver game.
     * Initializes GUI components and layout.
     */
    public GUIView() {
        super("Weaver Game"); // 窗口标题
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置关闭操作
        setLayout(new BorderLayout()); // 使用 BorderLayout 作为主布局管理器

        // --- 顶部面板：起始词和目标词 ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        initialWordLabel = new JLabel("Start Word: ____");
        targetWordLabel = new JLabel("Target Word: ____");
        topPanel.add(initialWordLabel);
        topPanel.add(Box.createHorizontalStrut(50)); // 添加水平间距
        topPanel.add(targetWordLabel);
        add(topPanel, BorderLayout.NORTH); // 将顶部面板添加到窗口顶部

        // --- 游戏面板：显示单词路径 ---
        gameBoardPanel = new JPanel();
        // 使用 BoxLayout 垂直排列每个单词面板
        gameBoardPanel.setLayout(new BoxLayout(gameBoardPanel, BoxLayout.Y_AXIS));
        // 添加一个滚动面板，以防单词过多超出可见区域
        JScrollPane scrollPane = new JScrollPane(gameBoardPanel);
        scrollPane.setPreferredSize(new Dimension(250, 300)); // 设置滚动面板的优选大小
        add(scrollPane, BorderLayout.CENTER); // 将滚动面板添加到窗口中心

        // --- 底部面板：包含虚拟键盘、控制按钮和消息标签 ---
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // 控制面板 (按钮和复选框)
        controlPanel = new JPanel(new FlowLayout());
        resetButton = new JButton("Reset");
        newGameButton = new JButton("New Game");
        showErrorsCheckBox = new JCheckBox("Show Errors");
        randomWordCheckBox = new JCheckBox("Random Words");
        // **移除 showPathCheckBox 的创建代码**
        // showPathCheckBox = new JCheckBox("Show Path");

        // **创建 Show Path 按钮**
        showPathButton = new JButton("Show Solution Path"); // **创建按钮实例**

        // 将按钮和复选框添加到控制面板
        controlPanel.add(resetButton);
        controlPanel.add(newGameButton);
        controlPanel.add(showErrorsCheckBox);
        controlPanel.add(randomWordCheckBox);
        controlPanel.add(showPathButton); // **将新按钮添加到控制面板**


        bottomPanel.add(controlPanel, BorderLayout.NORTH); // 将控制面板添加到底部面板顶部

        // **虚拟键盘面板和当前输入显示面板**
        // 将虚拟键盘和当前输入显示标签放在一个面板中
        JPanel keyboardAndInputPanel = new JPanel(new BorderLayout()); // 使用 BorderLayout 放置两个组件
        keyboardPanel = createKeyboardPanel(); // 创建虚拟键盘按钮面板
        keyboardAndInputPanel.add(keyboardPanel, BorderLayout.CENTER); // 将虚拟键盘面板添加到中心

        // **添加用于显示当前输入的 JLabel**
        currentInputDisplayLabel = new JLabel("Current Input: "); // **初始化标签**
        JPanel inputDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // 使用 FlowLayout 使标签居中
        inputDisplayPanel.add(currentInputDisplayLabel);
        keyboardAndInputPanel.add(inputDisplayPanel, BorderLayout.NORTH); // 将输入显示面板添加到北部

        bottomPanel.add(keyboardAndInputPanel, BorderLayout.CENTER); // 将包含键盘和输入显示的面板添加到底部面板中心

        // 消息标签面板 (位于虚拟键盘下方)
        JPanel messagePanel = new JPanel(new FlowLayout());
        messageLabel = new JLabel("Enter your first word."); // 初始化消息标签的文本
        messageLabel.setForeground(Color.BLUE); // 可选：设置消息颜色
        messagePanel.add(messageLabel);
        bottomPanel.add(messagePanel, BorderLayout.SOUTH); // 将消息面板添加到底部面板底部

        add(bottomPanel, BorderLayout.SOUTH); // 将整个底部面板添加到窗口底部

        // --- 窗口设置 ---
        pack(); // 自动调整窗口大小以适应内容
        setLocationRelativeTo(null); // 窗口居中显示
        setVisible(true); // 设置窗口可见

        // 不再需要设置窗口可聚焦以接收物理键盘事件
        // setFocusable(true);
        // requestFocusInWindow();


        // 按钮初始状态设置
        resetButton.setEnabled(false); // 初始时重置按钮禁用
        // showPathButton 可以根据游戏状态启用/禁用，这里保持始终启用简单处理
    }

    /**
     * Sets the controller for this view.
     * The controller will handle user interactions.
     * Event listeners are hooked up here, linking GUI component actions to controller methods.
     *
     * @param controller The GUIController instance.
     */

    public void setController(GUIController controller) {
        this.controller = controller;
        // 添加按钮和复选框的 ActionListeners，链接到 Controller 的处理方法
        resetButton.addActionListener(e -> controller.handleResetAction());
        newGameButton.addActionListener(e -> controller.handleNewGameAction());
        showErrorsCheckBox.addActionListener(e -> controller.handleShowErrorsFlag(showErrorsCheckBox.isSelected()));
        randomWordCheckBox.addActionListener(e -> controller.handleRandomWordFlag(randomWordCheckBox.isSelected()));
        // **移除 showPathCheckBox 的 ActionListener**
        // showPathCheckBox.addActionListener(e -> controller.handleShowPathFlag(showPathCheckBox.isSelected())); // **移除这行**

        // **为新的 showPathButton 添加 ActionListener**
        showPathButton.addActionListener(e -> {
            if (this.controller != null) { // 确保 controller 不为 null
                this.controller.handleShowPathAction(); // **调用 Controller 中处理 Show Path 按钮点击的新方法**
            }
        });


        // **移除物理键盘 KeyListener 的添加代码**
        // addKeyListener(controller.getKeyListener()); // 移除或注释掉这行

        // **为虚拟键盘按钮添加 ActionListeners**
        // 遍历 createKeyboardPanel 创建的按钮，并添加监听器
        if (keyboardPanel != null) { // 确保 keyboardPanel 已经创建
            for (Component panelComponent : keyboardPanel.getComponents()) {
                if (panelComponent instanceof JPanel) { // 每一行是一个 JPanel
                    JPanel rowPanel = (JPanel) panelComponent;
                    for (Component buttonComponent : rowPanel.getComponents()) {
                        if (buttonComponent instanceof JButton) { // 每个按钮是 JButton
                            JButton button = (JButton) buttonComponent;
                            // 使用 lambda 表达式，将按钮的文本传递给 Controller 的方法
                            button.addActionListener(e -> {
                                if (this.controller != null) { // 确保 controller 不为 null
                                    // 调用 Controller 处理虚拟键盘按键的方法
                                    // Controller 需要区分字母键、Enter 键和删除键
                                    this.controller.handleVirtualKeyPress(button.getText());
                                }
                            });
                        }
                    }
                }
            }
        }
        // 不再需要请求窗口焦点以接收物理键盘输入
        // requestFocusInWindow();
    }

    /**
     * Creates the panel containing the virtual keyboard buttons.
     * Arranges buttons in rows using FlowLayout within a main GridLayout panel.
     * Buttons are labeled and styled.
     *
     * @return The JPanel for the keyboard.
     */
    private JPanel createKeyboardPanel() {
        // 主面板使用 GridLayout，垂直排列 3 行按钮
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5)); // 3 行, 1 列, 垂直和水平间距 5

        // 定义每行的按钮文本
        String[] row1 = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"};
        String[] row2 = {"A", "S", "D", "F", "G", "H", "J", "K", "L"};
        // 将 Backspace 按钮文本改为 "DEL" 更符合常用键盘布局
        String[] row3 = {"ENTER", "Z", "X", "C", "V", "B", "N", "M", "DEL"}; // 使用 "DEL" 表示退格键

        // 创建并添加每一行按钮面板
        panel.add(createKeyboardRow(row1));
        panel.add(createKeyboardRow(row2));
        panel.add(createKeyboardRow(row3));

        return panel; // 返回完整的虚拟键盘面板
    }

    /**
     * Creates a single row of keyboard buttons.
     * Uses FlowLayout to center buttons within the row and sets button sizes.
     *
     * @param letters The array of strings for the buttons in this row (e.g., {"Q", "W", ...}).
     * @return The JPanel for the keyboard row.
     */
    private JPanel createKeyboardRow(String[] letters) {
        // 使用 FlowLayout 水平排列按钮，居中对齐，设置按钮间距
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3)); // 居中对齐，水平和垂直间距 3
        for (String letter : letters) {
            JButton button = new JButton(letter); // 创建按钮
            // 根据按钮文本长度设置按钮的优选大小，以确保文本完整显示
            if (letter.length() == 1 || letter.equals("DEL")) { // 对于单个字母或 "DEL" 键
                button.setPreferredSize(new Dimension(45, 45)); // 较小的正方形按钮
            } else { // 对于特殊键如 "ENTER"
                button.setPreferredSize(new Dimension(80, 45)); // 较宽的按钮
            }
            // 可选：调整按钮字体大小
            // button.setFont(new Font("Arial", Font.PLAIN, 16));
            row.add(button); // 将按钮添加到行面板
        }
        return row; // 返回一行按钮面板
    }

    /**
     * Updates the game board display with the current player's path and validation results.
     * Clears the existing display and adds new panels for each word in the path,
     * coloring letters based on the validation results.
     *
     * @param path    The list of words in the current game path (including the initial word).
     * @param results The list of validation results for each word in the path (excluding the initial word).
     */
    private void updateGameBoard(ArrayList<String> path, ArrayList<ValidationResult> results) {
        gameBoardPanel.removeAll(); // 移除游戏面板中所有的旧组件 (单词面板)

        // 调试打印接收到的路径和结果信息
        System.out.println("DEBUG: Entering updateGameBoard.");
        System.out.println("DEBUG: Received path: " + path);
        System.out.println("DEBUG: Received results: " + results);
        System.out.println("DEBUG: Path size: " + (path != null ? path.size() : 0) + ", Results size: " + (results != null ? results.size() : 0));

        // 显示路径中的第一个词 (起始词)
        // 起始词没有对应的玩家输入验证结果
        if (path != null && !path.isEmpty()) {
            JPanel initialWordPanel = createWordPanel(path.get(0), null); // 第一个词传递 null 作为结果
            gameBoardPanel.add(initialWordPanel); // 将起始词面板添加到游戏面板
            System.out.println("DEBUG: Added initial word panel for: " + path.get(0));
        }

        // 显示路径中的后续词语 (玩家输入的词) 及其验证结果
        // 从路径中的第二个词 (索引 1) 开始遍历
        for (int i = 1; i < (path != null ? path.size() : 0); i++) {
            String word = path.get(i); // 获取当前单词
            // 获取对应的验证结果 (results 列表比 path 列表少一个元素，所以索引是 i-1)
            ValidationResult result = (results != null && results.size() > i - 1) ?
                    results.get(i - 1) : null;

            if (result != null) {
                JPanel wordPanel = createWordPanel(word, result); // 创建带有验证结果的单词面板
                gameBoardPanel.add(wordPanel); // 将单词面板添加到游戏面板
                System.out.println("DEBUG: Added word panel for: " + word + " with result.");
            } else {
                // 如果由于某种原因没有对应的验证结果，显示没有颜色的单词面板
                JPanel wordPanel = createWordPanel(word, null); // 传递 null，不显示颜色
                gameBoardPanel.add(wordPanel);
                System.out.println("DEBUG: Added word panel for: " + word + " without result (unexpected).");
            }
        }

        // 添加所有单词面板后，重新布局并重绘游戏面板
        gameBoardPanel.revalidate(); // 重新计算布局
        gameBoardPanel.repaint(); // 重绘组件

        System.out.println("DEBUG: Exiting updateGameBoard.");
    }

    /**
     * Creates a JPanel to display a single word, coloring letters based on ValidationResult.
     * Each letter is a JLabel with a background color and border.
     *
     * @param word   The word to display.
     * @param result The validation result for this word (can be null for the initial word or invalid inputs not added to results).
     * @return The JPanel displaying the word. Returns an empty panel if the input word is null.
     */
    private JPanel createWordPanel(String word, ValidationResult result) {
        // 使用 FlowLayout 水平排列字母 JLabel，设置间距
        JPanel wordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5)); // 居中对齐，水平和垂直间距 5

        // 获取字母状态 Map，如果 result 为 null (例如起始词)，则 letterStates 为 null
        Map<Integer, LetterState> letterStates = (result != null) ? result.getLetterStates() : null;

        // 如果传入的单词为 null，打印错误并返回一个空面板
        if (word == null) {
            System.err.println("Error: createWordPanel received null word.");
            return wordPanel; // 返回一个空的 JPanel
        }

        // 为单词中的每个字符创建 JLabel
        for (int i = 0; i < word.length(); i++) {
            char character = word.charAt(i);
            JLabel letterLabel = new JLabel(String.valueOf(character)); // 创建显示字母的 JLabel
            letterLabel.setPreferredSize(new Dimension(35, 35)); // 设置 JLabel 的大小，形成一个方框
            letterLabel.setHorizontalAlignment(SwingConstants.CENTER); // 字母居中显示
            letterLabel.setVerticalAlignment(SwingConstants.CENTER);
            letterLabel.setOpaque(true); // 必须设置为 true，背景色才能显示
            letterLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // 添加黑色边框，粗细为 1

            // 根据字母的状态设置背景颜色
            LetterState state = null;
            // 如果 letterStates 不为 null 且包含当前索引的键
            if (letterStates != null && letterStates.containsKey(i)) {
                state = letterStates.get(i); // 获取 LetterState
                // System.out.println("DEBUG: Index " + i + ", character '" + word.charAt(i) + "', State: " + state); // 调试打印状态
            }

            Color bgColor = Color.WHITE; // 默认背景颜色为白色

            // 根据获取到的状态设置背景颜色
            if (state != null) {
                switch (state) {
                    case CORRECT_POSITION:
                        bgColor = Color.GREEN; // 位置正确显示绿色
                        break;
                    case WRONG_POSITION:
                        bgColor = Color.YELLOW; // 位置错误但字母存在显示黄色
                        break;
                    case NOT_IN_WORD:
                        bgColor = Color.LIGHT_GRAY; // 字母不在目标词中显示浅灰色
                        break;
                    default:
                        // 如果状态是 LetterState.DEFAULT 或其他意外状态，保持白色
                        bgColor = Color.WHITE;
                        break;
                }
            } else {
                // 如果没有验证结果或状态信息，保持白色
                bgColor = Color.WHITE;
                // System.out.println("DEBUG: Index " + i + ", character '" + word.charAt(i) + "': No validation state."); // 调试打印无状态信息
            }

            letterLabel.setBackground(bgColor); // 设置 JLabel 的背景颜色
            wordPanel.add(letterLabel); // 将字母 JLabel 添加到单词面板中
        }
        // System.out.println("DEBUG: Exiting createWordPanel for word: " + word); // 调试打印退出信息
        return wordPanel; // 返回创建好的单词面板
    }


    /**
     * Clears the game board display by removing all word panels.
     */
    private void clearGameBoard() {
        gameBoardPanel.removeAll(); // 移除所有组件
        gameBoardPanel.revalidate(); // 重新计算布局
        gameBoardPanel.repaint(); // 重绘
        System.out.println("DEBUG: Game board cleared."); // 调试打印
    }

    /**
     * Sets the text for the message label at the bottom of the view.
     * Handles null messages by setting the label text to an empty string.
     *
     * @param message The message to display (e.g., instructions, errors, win message). Can be null.
     */
    public void setMessage(String message) {
        // 如果 message 为 null，设置标签文本为空字符串，避免显示 "null"
        messageLabel.setText(message != null ? message : "");
        // 调试打印设置的消息 (如果 message 为 null，打印 "NULL")
        System.out.println("DEBUG: Message set to: " + (message != null ? message : "NULL"));
    }

    /**
     * Sets the text for the initial word label.
     *
     * @param word The initial word.
     */
    public void setInitialWord(String word) {
        initialWordLabel.setText("Start Word: " + (word != null ? word : "____")); // 处理 null 值
        System.out.println("DEBUG: Initial word label set to: " + (word != null ? word : "NULL"));
    }

    /**
     * Sets the text for the target word label.
     *
     * @param word The target word.
     */
    public void setTargetWord(String word) {
        targetWordLabel.setText("Target Word: " + (word != null ? word : "____")); // 处理 null 值
        System.out.println("DEBUG: Target word label set to: " + (word != null ? word : "NULL"));
    }

    /**
     * Enables or disables the reset button.
     *
     * @param enabled True to enable, false to disable.
     */
    public void setResetButtonEnabled(boolean enabled) {
        resetButton.setEnabled(enabled);
        System.out.println("DEBUG: Reset button enabled: " + enabled);
    }

    /**
     * Updates the display area showing the current word being typed by the player.
     * @param currentInput The current string of characters typed. Can be null.
     */
    public void updateInputDisplay(String currentInput) {
        currentInputDisplayLabel.setText("Current Input: " + (currentInput != null ? currentInput : "")); // 处理 null 值
    }


    // --- Getters and Setters for Checkbox States (Used by Controller) ---
    // 这些方法用于 Controller 获取和设置复选框的状态

    public boolean isShowErrorsSelected() {
        return showErrorsCheckBox.isSelected();
    }

    public void setShowErrorsSelected(boolean selected) {
        showErrorsCheckBox.setSelected(selected);
        System.out.println("DEBUG: Show Errors checkbox set to: " + selected);
    }

    public boolean isRandomWordSelected() {
        return randomWordCheckBox.isSelected();
    }

    public void setRandomWordSelected(boolean selected) {
        randomWordCheckBox.setSelected(selected);
        System.out.println("DEBUG: Random Words checkbox set to: " + selected);
    }

    // **移除与 isShowPathSelected 和 setShowPathSelected 相关的代码**
    // public boolean isShowPathSelected() { ... } // 移除
    // public void setShowPathSelected(boolean selected) { ... } // 移除


    /**
     * This method is called when the observed Model changes.
     * It updates the GUI display based on the state received from the Model.
     * The state is expected to be wrapped in a Notification object.
     *
     * @param o   The observable object (WeaverModel).
     * @param arg An argument passed by the notifyObservers method (should be Notification).
     */
    @Override
    public void update(Observable o, Object arg) {
        System.out.println("DEBUG: Inside GUIView update method. Arg type: " + (arg != null ? arg.getClass().getName() : "null"));
        if (arg instanceof Notification) {
            Notification notification = (Notification) arg; // 将参数转换为 Notification 对象
            System.out.println("DEBUG: Arg is a Notification.");

            // **更新消息标签**
            // 消息内容是否显示取决于 Model 在 notifyUpdate 中根据 showErrorsFlag 发送了什么
            setMessage(notification.getRuntimeWarning() != null ? notification.getRuntimeWarning() : notification.getHint());

            // 如果 Notification 包含 GameState
            if (notification.containsGameState()) {
                GameState gameState = notification.getGameState(); // 获取 GameState
                System.out.println("DEBUG: Notification contains GameState.");
                System.out.println("DEBUG: GameState Initial: " + gameState.getInitialWord() + ", Target: " + gameState.getTargetWord());
                System.out.println("DEBUG: GameState Path size: " + (gameState.getPath() != null ? gameState.getPath().size() : 0) + ", Results size: " + (gameState.getResults() != null ? gameState.getResults().size() : 0) + ", Won: " + gameState.isWon());

                // 更新初始词和目标词标签
                setInitialWord(gameState.getInitialWord());
                setTargetWord(gameState.getTargetWord());

                // **更新游戏面板显示**
                // updateGameBoard 现在只负责显示玩家当前的路径和结果，不显示完整路径
                updateGameBoard(gameState.getPath(), gameState.getResults());

                // 根据玩家路径长度决定是否启用重置按钮 (路径包含起始词，所以长度 > 1 表示有玩家输入了)
                setResetButtonEnabled(gameState.getPath() != null && gameState.getPath().size() > 1);

                // 处理游戏胜利状态
                if (gameState.isWon()) {
                    System.out.println("DEBUG: GameState indicates won.");
                    // 胜利消息已由 Model 在 notifyUpdate 中设置，并由上面的 setMessage 控制显示。
                    // 如果游戏胜利需要禁用输入，可以在这里实现 (例如禁用虚拟键盘按钮)
                    // disableUserInput(); // 示例方法 (需要实现)
                }
            } else {
                System.out.println("DEBUG: Notification does not contain GameState.");
                // 处理只包含消息而不包含 GameState 的通知 (消息已在上面处理)
            }
        } else {
            System.out.println("DEBUG: Update received unexpected argument type: " + (arg != null ? arg.getClass().getName() : "null"));
            // 处理接收到的参数不是 Notification 的情况
        }

        // 不再需要请求窗口焦点以接收物理键盘输入
        // requestFocusInWindow();
    }

    // Method to disable user input after winning (optional, needs implementation)
    // 这个方法用于在游戏胜利后禁用玩家输入，例如禁用虚拟键盘按钮
    // private void disableUserInput() {
    //     if (keyboardPanel != null) {
    //         // 遍历键盘面板中的组件，禁用所有 JButton
    //         for (Component panelComponent : keyboardPanel.getComponents()) {
    //             if (panelComponent instanceof JPanel) {
    //                 JPanel rowPanel = (JPanel) panelComponent;
    //                 for (Component buttonComponent : rowPanel.getComponents()) {
    //                     if (buttonComponent instanceof JButton) {
    //                         JButton button = (JButton) buttonComponent;
    //                         button.setEnabled(false); // 禁用按钮
    //                     }
    //                 }
    //             }
    //         }
    //     }
    // }
}