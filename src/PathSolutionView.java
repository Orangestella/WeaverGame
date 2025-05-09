// File: PathSolutionView.java
// Create this new file

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;




// PathSolutionView 类继承 JFrame (用于创建独立的窗口)
public class PathSolutionView extends JFrame { // 您也可以选择继承 JDialog，JDialog 更适合模态弹出窗口

    private final ArrayList<String> solutionPath; // 存储解决方案路径
    private final String targetWord; // 存储目标词
    private final ArrayList<String> dictionary; // 存储字典 (用于路径步骤验证显示)

    // 构造函数，接收解决方案路径、目标词和字典
    public PathSolutionView(ArrayList<String> solutionPath, String targetWord, ArrayList<String> dictionary) {
        this.solutionPath = solutionPath;
        this.targetWord = targetWord;
        this.dictionary = dictionary;

        // 设置窗口属性
        // 设置窗口标题，显示起始词和目标词
        if (solutionPath != null && !solutionPath.isEmpty()) {
            setTitle("Solution Path: " + solutionPath.get(0) + " to " + (targetWord != null ? targetWord : "____"));
        } else {
            setTitle("Solution Path"); // 如果路径为空，设置一个默认标题
        }
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 设置关闭操作：关闭当前窗口而不影响主程序
        // 使用 BoxLayout 垂直排列组件，每个组件是一行的单词面板
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // **获取路径中每个单词的验证结果 (对照目标词)，用于显示颜色**
        // 调用 PathFinder.getValidations 方法
        // 根据 PathFinder 的修改，它接收 (目标词, 路径, 字典)
        ArrayList<ValidationResult> validationResults = PathFinder.getValidations(this.targetWord, this.solutionPath, this.dictionary); // **调用 PathFinder 获取验证结果列表**

        // **显示路径中的每个单词及其验证结果**
        // 路径的第一个词 (起始词) 没有前面的玩家输入验证结果，特殊处理
        if (this.solutionPath != null && !this.solutionPath.isEmpty()) {
            // 为第一个词创建面板，验证结果为 null
            add(createWordPanel(this.solutionPath.get(0), null));

            // 从路径中的第二个词开始 (索引 1)，遍历并显示单词面板
            // 每个词对应 validationResults 列表中的一个结果 (索引是 i-1)
            for (int i = 1; i < this.solutionPath.size(); i++) {
                String currentWord = this.solutionPath.get(i); // 获取当前单词
                // 获取对应的验证结果，确保索引不越界
                ValidationResult result = (validationResults != null && validationResults.size() > i - 1) ?
                        validationResults.get(i - 1) : null;

                // 为当前单词创建面板，并传入其验证结果
                add(createWordPanel(currentWord, result));
            }
        }


        // 添加一些垂直方向的填充，让单词面板靠顶部对齐
        add(Box.createVerticalGlue());

        pack(); // 自动调整窗口大小以适应内容
        setLocationRelativeTo(null); // 窗口居中显示
    }

    /**
     * Helper method: 创建一个显示单个单词及其字母颜色状态的 JPanel.
     * 每个字母是一个 JLabel，带有背景颜色和边框。
     * 这个方法与 GUIView 中的 createWordPanel 方法非常相似。
     *
     * @param word   要显示的单词。
     * @param result 该单词的验证结果 (对于路径中的第一个词，可以为 null)。
     * @return 显示该单词的 JPanel。如果输入的单词为 null，返回一个空面板。
     */
    private JPanel createWordPanel(String word, ValidationResult result) {
        // 使用 FlowLayout 水平排列 JLabel，居中对齐，设置字母间距
        JPanel wordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5)); // 居中对齐，水平和垂直间距 5

        // 获取字母状态 Map，如果 result 为 null (例如起始词)，则 letterStates 为 null
        Map<Integer, LetterState> letterStates = (result != null) ? result.getLetterStates() : null;

        // 如果传入的单词为 null，打印错误并返回一个空面板
        if (word == null) {
            System.err.println("Error: PathSolutionView.createWordPanel received null word.");
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
            }

            letterLabel.setBackground(bgColor); // 设置 JLabel 的背景颜色
            wordPanel.add(letterLabel); // 将字母 JLabel 添加到单词面板中
        }
        return wordPanel; // 返回创建好的单词面板
    }


}