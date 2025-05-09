// File: GUIView.java
// Based on code_2.txt 提供的当前代码

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

// 确保这些类在正确的包中或已正确导入
// import your_package_name.GameState;
// import your_package_name.ValidationResult;
// import your_package_name.LetterState;
// import your_package_name.Notification;
// import your_package_name.GUIController;


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

    // 已经移除与 showPathCheckBox 相关的代码

    // 添加 Show Path 按钮
    private JButton showPathButton; // 添加显示路径按钮字段

    private GUIController controller; // Controller 的引用

    // 添加用于显示当前玩家输入的 JLabel
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

        // 创建 Show Path 按钮
        showPathButton = new JButton("Show Solution Path"); // 创建按钮实例

        // 将按钮和复选框添加到控制面板
        controlPanel.add(resetButton);
        controlPanel.add(newGameButton);
        controlPanel.add(showErrorsCheckBox);
        controlPanel.add(randomWordCheckBox);
        controlPanel.add(showPathButton); // 将新按钮添加到控制面板


        bottomPanel.add(controlPanel, BorderLayout.NORTH); // 将控制面板添加到底部面板顶部

        // **虚拟键盘面板和当前输入显示面板**
        // 将虚拟键盘和当前输入显示标签放在一个面板中
        JPanel keyboardAndInputPanel = new JPanel(new BorderLayout()); // 使用 BorderLayout 放置两个组件
        keyboardPanel = createKeyboardPanel(); // 创建虚拟键盘按钮面板
        keyboardAndInputPanel.add(keyboardPanel, BorderLayout.CENTER); // 将虚拟键盘面板添加到中心

        // 添加用于显示当前输入的 JLabel
        currentInputDisplayLabel = new JLabel("Current Input: "); // 初始化标签
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

        // 为新的 showPathButton 添加 ActionListener
        showPathButton.addActionListener(e -> {
            if (this.controller != null) {
                this.controller.handleShowPathAction();
            }
        });

        // 移除物理键盘 KeyListener 的添加代码 (您应该已经移除了)
        // addKeyListener(controller.getKeyListener());

        // 为虚拟键盘按钮添加 ActionListeners
        if (keyboardPanel != null) {
            for (Component panelComponent : keyboardPanel.getComponents()) {
                if (panelComponent instanceof JPanel) {
                    JPanel rowPanel = (JPanel) panelComponent;
                    for (Component buttonComponent : rowPanel.getComponents()) {
                        if (buttonComponent instanceof JButton) {
                            JButton button = (JButton) buttonComponent;
                            button.addActionListener(e -> {
                                if (this.controller != null) {
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
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));

        String[] row1 = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"};
        String[] row2 = {"A", "S", "D", "F", "G", "H", "J", "K", "L"};
        String[] row3 = {"ENTER", "Z", "X", "C", "V", "B", "N", "M", "DEL"};

        panel.add(createKeyboardRow(row1));
        panel.add(createKeyboardRow(row2));
        panel.add(createKeyboardRow(row3));

        return panel;
    }

    /**
     * Creates a single row of keyboard buttons.
     * Uses FlowLayout to center buttons within the row and sets button sizes.
     *
     * @param letters The array of strings for the buttons in this row.
     * @return The JPanel for the keyboard row.
     */
    private JPanel createKeyboardRow(String[] letters) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        for (String letter : letters) {
            JButton button = new JButton(letter);
            if (letter.length() == 1) {
                button.setPreferredSize(new Dimension(45, 45));
            } else {
                button.setPreferredSize(new Dimension(80, 45));
            }
            row.add(button);
        }
        return row;
    }

    /**
     * Updates the game board display with the current player's path and validation results.
     * Clears the existing display and adds new panels for each word in the path,
     * coloring letters based on the validation results.
     *
     * @param path    The list of words in the current game path.
     * @param results The list of validation results for each word in the path.
     */
    private void updateGameBoard(ArrayList<String> path, ArrayList<ValidationResult> results) {
        gameBoardPanel.removeAll();

        System.out.println("DEBUG: Entering updateGameBoard.");
        System.out.println("DEBUG: Received path: " + path);
        System.out.println("DEBUG: Received results: " + results);
        System.out.println("DEBUG: Path size: " + (path != null ? path.size() : 0) + ", Results size: " + (results != null ? results.size() : 0));

        if (path != null && !path.isEmpty()) {
            JPanel initialWordPanel = createWordPanel(path.get(0), null);
            gameBoardPanel.add(initialWordPanel);
            System.out.println("DEBUG: Added initial word panel for: " + path.get(0));
        }

        for (int i = 1; i < (path != null ? path.size() : 0); i++) {
            String word = path.get(i);
            ValidationResult result = (results != null && results.size() > i - 1) ?
                    results.get(i - 1) : null;

            if (result != null) {
                JPanel wordPanel = createWordPanel(word, result);
                gameBoardPanel.add(wordPanel);
                System.out.println("DEBUG: Added word panel for: " + word + " with result.");
            } else {
                JPanel wordPanel = createWordPanel(word, null);
                gameBoardPanel.add(wordPanel);
                System.out.println("DEBUG: Added word panel for: " + word + " without result (unexpected).");
            }
        }

        gameBoardPanel.revalidate();
        gameBoardPanel.repaint();

        System.out.println("DEBUG: Exiting updateGameBoard.");
    }

    /**
     * Creates a JPanel to display a single word, coloring letters based on ValidationResult.
     * Each letter is a JLabel with a background color and border.
     *
     * @param word   The word to display.
     * @param result The validation result for this word.
     * @return The JPanel displaying the word.
     */
    private JPanel createWordPanel(String word, ValidationResult result) {
        JPanel wordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        Map<Integer, LetterState> letterStates = (result != null) ? result.getLetterStates() : null;

        if (word == null) {
            System.err.println("Error: createWordPanel received null word.");
            return wordPanel;
        }

        for (int i = 0; i < word.length(); i++) {
            JLabel letterLabel = new JLabel(String.valueOf(word.charAt(i)));
            letterLabel.setPreferredSize(new Dimension(35, 35));
            letterLabel.setHorizontalAlignment(SwingConstants.CENTER);
            letterLabel.setVerticalAlignment(SwingConstants.CENTER);
            letterLabel.setOpaque(true);
            letterLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

            LetterState state = null;
            if (letterStates != null && letterStates.containsKey(i)) {
                state = letterStates.get(i);
            }

            Color bgColor = Color.WHITE;

            if (state != null) {
                switch (state) {
                    case CORRECT_POSITION:
                        bgColor = Color.GREEN;
                        break;
                    case WRONG_POSITION:
                        bgColor = Color.YELLOW;
                        break;
                    case NOT_IN_WORD:
                        bgColor = Color.LIGHT_GRAY;
                        break;
                    default:
                        bgColor = Color.WHITE;
                        break;
                }
            } else {
                bgColor = Color.WHITE;
            }

            letterLabel.setBackground(bgColor);
            wordPanel.add(letterLabel);
        }
        return wordPanel;
    }

    /**
     * Clears the game board display by removing all word panels.
     */
    private void clearGameBoard() {
        gameBoardPanel.removeAll();
        gameBoardPanel.revalidate();
        gameBoardPanel.repaint();
        System.out.println("DEBUG: Game board cleared.");
    }

    /**
     * Sets the text for the message label at the bottom of the view.
     *
     * @param message The message to display.
     */
    public void setMessage(String message) {
        messageLabel.setText(message != null ? message : "");
        System.out.println("DEBUG: Message set to: " + (message != null ? message : "NULL"));
    }

    /**
     * Sets the text for the initial word label.
     *
     * @param word The initial word.
     */
    public void setInitialWord(String word) {
        initialWordLabel.setText("Start Word: " + (word != null ? word : "____"));
        System.out.println("DEBUG: Initial word label set to: " + (word != null ? word : "NULL"));
    }

    /**
     * Sets the text for the target word label.
     *
     * @param word The target word.
     */
    public void setTargetWord(String word) {
        targetWordLabel.setText("Target Word: " + (word != null ? word : "____"));
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
     * @param currentInput The current string of characters typed.
     */
    public void updateInputDisplay(String currentInput) {
        currentInputDisplayLabel.setText("Current Input: " + (currentInput != null ? currentInput : ""));
    }


    // --- Getters and Setters for Checkbox States (Used by Controller) ---

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
            Notification notification = (Notification) arg;
            System.out.println("DEBUG: Arg is a Notification.");

            // Update messages first
            setMessage(notification.getRuntimeWarning() != null ? notification.getRuntimeWarning() : notification.getHint());

            if (notification.containsGameState()) {
                GameState gameState = notification.getGameState();
                System.out.println("DEBUG: Notification contains GameState.");
                System.out.println("DEBUG: GameState Initial: " + gameState.getInitialWord() + ", Target: " + gameState.getTargetWord());
                System.out.println("DEBUG: GameState Path size: " + (gameState.getPath() != null ? gameState.getPath().size() : 0) + ", Results size: " + (gameState.getResults() != null ? gameState.getResults().size() : 0) + ", Won: " + gameState.isWon());

                // Update initial and target word labels
                setInitialWord(gameState.getInitialWord());
                setTargetWord(gameState.getTargetWord());

                // Update the game board display
                updateGameBoard(gameState.getPath(), gameState.getResults());

                // Enable reset button after the first player input
                setResetButtonEnabled(gameState.getPath() != null && gameState.getPath().size() > 1);

                // **Control virtual keyboard enabled state based on game won state**
                if (gameState.isWon()) {
                    System.out.println("DEBUG: GameState indicates won. Disabling keyboard.");
                    setKeyboardEnabled(false); // Disable keyboard if game is won
                } else {
                    System.out.println("DEBUG: GameState indicates ongoing or reset. Enabling keyboard.");
                    setKeyboardEnabled(true); // Enable keyboard if game is not won
                }

            } else {
                System.out.println("DEBUG: Notification does not contain GameState.");
                // Handle notifications that are just messages
            }
        } else {
            System.out.println("DEBUG: Update received unexpected argument type: " + (arg != null ? arg.getClass().getName() : "null"));
        }
    }

    /**
     * Enables or disables the virtual keyboard buttons.
     * This method iterates through the keyboard panel components and sets their enabled state.
     *
     * @param enabled True to enable, false to disable.
     */
    private void setKeyboardEnabled(boolean enabled) {
        System.out.println("DEBUG: Attempting to set virtual keyboard enabled state to: " + enabled); // Debug print

        if (keyboardPanel != null) {
            // Iterate through all components in the keyboard panel
            // The keyboard panel is a GridLayout of JPanels (rows)
            for (Component panelComponent : keyboardPanel.getComponents()) {
                // Check if the component is a JPanel (representing a row)
                if (panelComponent instanceof JPanel) {
                    JPanel rowPanel = (JPanel) panelComponent;
                    // Iterate through buttons in each row panel
                    for (Component buttonComponent : rowPanel.getComponents()) {
                        // Check if the component is a JButton (a keyboard key)
                        if (buttonComponent instanceof JButton) {
                            JButton button = (JButton) buttonComponent;
                            button.setEnabled(enabled); // **Set the enabled state of the button**
                        }
                    }
                }
            }
            System.out.println("DEBUG: Virtual keyboard enabled state set to: " + enabled); // Confirm state was set
        } else {
            System.err.println("Error: keyboardPanel is null in setKeyboardEnabled."); // Handle null panel case
        }
    }
}