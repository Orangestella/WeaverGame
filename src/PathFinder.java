import java.util.*;

public class PathFinder {
    public static ArrayList<String> findPathByBFS(String initial, String target, ArrayList<String> dictionary) {

        Set<String> dictSet = new HashSet<>(dictionary);
        if (!dictSet.contains(target)) {
            return new ArrayList<>();
        }
        if (initial.equals(target)) {
            ArrayList<String> result = new ArrayList<>();
            result.add(initial);
            return result;
        }


        Map<String, List<String>> patternMap = new LinkedHashMap<>();
        for (String word : dictSet) {
            for (int i = 0; i < word.length(); i++) {
                String pattern = word.substring(0, i) + '*' + word.substring(i + 1);
                patternMap.computeIfAbsent(pattern, k -> new ArrayList<>()).add(word);
            }
        }

        Queue<ArrayList<String>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        ArrayList<String> initialPath = new ArrayList<>();
        initialPath.add(initial);
        queue.add(initialPath);
        visited.add(initial);

        while (!queue.isEmpty()) {
            ArrayList<String> currentPath = queue.poll();
            String currentWord = currentPath.get(currentPath.size() - 1);

            for (int i = 0; i < currentWord.length(); i++) {
                String pattern = currentWord.substring(0, i) + '*' + currentWord.substring(i + 1);
                List<String> neighbors = patternMap.getOrDefault(pattern, new ArrayList<>());
                for (String neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        ArrayList<String> newPath = new ArrayList<>(currentPath);
                        newPath.add(neighbor);
                        if (neighbor.equals(target)) {
                            return newPath;
                        }
                        queue.add(newPath);
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    public static ArrayList<ValidationResult> getValidations(String target, ArrayList<String> path, ArrayList<String> dictionary) {
        ArrayList<ValidationResult> validationResults = new ArrayList<>();
        // 如果路径为 null 或空，或者只包含起始词 (长度 <= 1)，则没有需要验证的步骤
        if (path == null || path.size() <= 1) {
            return validationResults; // 返回一个空列表
        }

        // **在 PathFinder 内部创建一个 BasicValidator 实例用于路径步骤的验证显示**
        WordValidator pathStepValidator = new BasicValidator(); // **确保 BasicValidator 可以被正确实例化**

        // 从路径的第二个词开始 (索引 1) 遍历，计算每个词的验证结果
        for (int i = 1; i < path.size(); i++) {
            String currentWord = path.get(i); // 获取当前路径中的单词
            // 使用内部创建的 BasicValidator 验证当前词是否符合目标词的规则
            // **确保 BasicValidator.validate(inputWord, targetWord, dictionary) 的参数顺序正确**
            // 在这里，currentWord 是本次验证的“输入词”，target 是最终的“目标词”
            ValidationResult result = pathStepValidator.validate(currentWord, target, dictionary); // **使用内部 Validator 进行验证**
            validationResults.add(result); // 将验证结果添加到列表中
        }
        return validationResults; // 返回验证结果列表
    }

}
