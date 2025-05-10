import java.util.*;

/**
 * A utility class for finding word transformation paths and validating them.
 * Provides methods to:
 * - Find a path from an initial word to a target word using BFS
 * - Generate validation results for each step in the path
 */
public class PathFinder {

    /**
     * Finds the shortest transformation path from the initial word to the target word,
     * using Breadth-First Search (BFS) over the dictionary of valid words.
     *
     * @param initial   The starting word
     * @param target    The target word to reach
     * @param dictionary A list of valid words that can be used in transformations
     * @return An ArrayList containing the sequence of words from initial to target, or an empty list if no path exists
     */
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

    /**
     * Generates validation results for each word in the path.
     * Uses a WordValidator to validate each step against the target word.
     *
     * @param target    The final target word
     * @param path      The sequence of words leading to the target
     * @param dictionary The dictionary of valid words
     * @return An ArrayList of ValidationResult objects corresponding to each step in the path
     */
    public static ArrayList<ValidationResult> getValidations(String target, ArrayList<String> path, ArrayList<String> dictionary) {
        ArrayList<ValidationResult> validationResults = new ArrayList<>();
        if (path == null || path.size() <= 1) {
            return validationResults;
        }

        WordValidator pathStepValidator = new BasicValidator();


        for (int i = 1; i < path.size(); i++) {
            String currentWord = path.get(i);

            ValidationResult result = pathStepValidator.validate(currentWord, target, dictionary); // **使用内部 Validator 进行验证**
            validationResults.add(result);
        }
        return validationResults;
    }

}
