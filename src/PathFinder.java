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

    public static ArrayList<ValidationResult> getValidations(String target, ArrayList<String> path, ArrayList<String> dictionary, WordValidator validator) {
        ArrayList<ValidationResult> validationResults = new ArrayList<>();
        if (path.isEmpty())
            throw new RuntimeException("Path not found");
        for (String word : path)
            validationResults.add(validator.validate(word, target, dictionary));
        return validationResults;
    }

}
