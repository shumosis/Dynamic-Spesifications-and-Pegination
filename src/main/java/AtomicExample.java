public class AtomicExample {

    public static void main(String[] args) {

        System.out.println(findOriginalString("szizbibzzbb")); // Expected: "izizibib"
        System.out.println(findOriginalString("sibiciab"));    // Expected: "notpossible"
    }

    public static String findOriginalString(String modifiedString) {
        String processedString = removeCharacter(modifiedString, 's');

        String repeatingPattern = findRepeatingPattern(processedString);

        if (repeatingPattern == null) {
            return "notpossible";
        }

        String originalString = buildOriginalString(modifiedString, repeatingPattern);

        if (originalString == null) {
            return "notpossible";
        }

        return originalString;
    }

    private static String removeCharacter(String str, char ch) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c != ch) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String findRepeatingPattern(String str) {
        int length = str.length();

        for (int i = 1; i <= length / 2; i++) {
            String pattern = str.substring(0, i);
            if (isRepeatingPattern(str, pattern)) {
                return pattern;
            }
        }

        return null;
    }

    private static boolean isRepeatingPattern(String str, String pattern) {
        int patternLength = pattern.length();

        for (int i = 0; i < str.length(); i += patternLength) {
            if (i + patternLength > str.length() || !str.substring(i, i + patternLength).equals(pattern)) {
                return false;
            }
        }

        return true;
    }

    private static String buildOriginalString(String modifiedString, String pattern) {
        StringBuilder sb = new StringBuilder();
        int patternIndex = 0;

        for (char c : modifiedString.toCharArray()) {
            if (c == 's') {
                continue;
            }

            if (c == pattern.charAt(patternIndex)) {
                sb.append(c);
                patternIndex = (patternIndex + 1) % pattern.length();
            } else {
                return null;
            }
        }

        return sb.toString();
    }
}
