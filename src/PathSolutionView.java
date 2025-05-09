// PathSolutionView.java

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;


public class PathSolutionView extends JFrame {

    private JPanel contentPanel; // 用于存放路径中每个单词的面板

    public PathSolutionView() {
        super("Solution Path"); // 窗口标题
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); // 点击关闭按钮时隐藏窗口，而不是销毁
        setSize(300, 400); // 窗口默认大小

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); // 使用 BoxLayout 垂直排列单词面板

        JScrollPane scrollPane = new JScrollPane(contentPanel); // 为内容面板添加滚动条
        add(scrollPane, BorderLayout.CENTER); // 将滚动面板添加到窗口中心

        // 可选：添加一些内边距
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    /**
     * 显示解决方案路径及其验证结果。
     *
     * @param path 包含从起始词到目标词的完整路径的列表。
     * @param results 路径中除起始词外每个单词的验证结果列表。
     */
    public void displayPath(ArrayList<String> path, ArrayList<ValidationResult> results) {
        contentPanel.removeAll(); // 清空之前显示的内容

        if (path == null || path.isEmpty()) {
            contentPanel.add(new JLabel("No path found.")); // 如果没有找到路径，显示提示
        } else {
            // 显示起始词 (起始词本身没有验证结果需要显示)
            contentPanel.add(createWordPanel(path.get(0), null)); // 调用辅助方法创建单词面板

            // 显示路径中后续的单词及其验证结果
            // results 列表通常包含 path 中从索引 1 开始的单词的验证结果
            for (int i = 0; i < results.size(); i++) {
                String word = path.get(i + 1); // 获取当前单词 (路径中的索引 i+1)
                ValidationResult result = results.get(i); // 获取对应的验证结果 (结果列表中的索引 i)
                contentPanel.add(createWordPanel(word, result)); // 调用辅助方法创建单词面板
            }
        }

        contentPanel.revalidate(); // 重新布局内容
        contentPanel.repaint(); // 重绘面板
    }

    /**
     * 辅助方法：创建一个显示单词及其着色结果的 JPanel。
     *
     * @param word 要显示的单词。
     * @param result 对应的验证结果 (如果为 null，则不着色)。
     * @return 包含着色字母标签的 JPanel。
     */
    private JPanel createWordPanel(String word, ValidationResult result) {
        JPanel wordPanel = new JPanel();
        // 使用 FlowLayout 水平排列字母标签
        wordPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0)); // 左对齐，字母间水平间距 2 像素

        if (word == null) {
            wordPanel.add(new JLabel("null")); // 处理单词为 null 的情况
            return wordPanel;
        }

        // 获取字母状态 Map，如果 result 为 null，则 letterStates 也为 null
        Map<Integer, LetterState> letterStates = (result != null) ? result.getLetterStates() : null;

        for (int i = 0; i < word.length(); i++) {
            JLabel letterLabel = new JLabel(String.valueOf(word.charAt(i)));
            letterLabel.setOpaque(true); // 必须设置为 true 才能显示背景颜色
            letterLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // 添加黑色边框
            letterLabel.setPreferredSize(new Dimension(20, 20)); // 设置固定的尺寸
            letterLabel.setHorizontalAlignment(SwingConstants.CENTER); // 水平居中
            letterLabel.setVerticalAlignment(SwingConstants.CENTER); // 垂直居中

            Color bgColor = Color.WHITE; // 默认背景颜色为白色

            // 如果有验证结果，根据 LetterState 设置背景颜色
            if (letterStates != null && letterStates.containsKey(i)) {
                LetterState state = letterStates.get(i);
                switch (state) {
                    case CORRECT_POSITION:
                        bgColor = Color.GREEN;
                        break;
                    case WRONG_POSITION:
                        bgColor = Color.YELLOW;
                        break;
                    case NOT_IN_WORD:
                        bgColor = Color.LIGHT_GRAY; // 浅灰色
                        break;
                    // 可以添加 default case 处理其他可能的 LetterState
                }
            } else {
                // 如果没有验证结果 (例如，起始词)，使用默认背景颜色
                bgColor = Color.WHITE;
            }

            letterLabel.setBackground(bgColor);
            wordPanel.add(letterLabel); // 将字母标签添加到单词面板
        }
        return wordPanel; // 返回包含单词着色字母的面板
    }
}