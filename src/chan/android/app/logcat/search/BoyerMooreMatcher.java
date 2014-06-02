package chan.android.app.logcat.search;

public class BoyerMooreMatcher extends AbstractMatcher {

    private int[] bad;

    protected BoyerMooreMatcher(char[] pattern) {
        super(pattern);
        bad = fillHeuristicChar(pattern);
    }


    @Override
    public boolean match(char[] text) {
        final int M = pattern.length;
        final int N = text.length;

        // Shift of pattern with respect to text
        int shift = 0;
        int diff = N - M;
        int i;
        while (shift <= diff) {
            i = M - 1;

            // Keep reducing index j of pattern while characters of
            // pattern and text are matching at this shift
            while (i >= 0 && pattern[i] == text[shift + i]) {
                i--;
            }

            // If the pattern is present at current shift, then index i
            // will become -1 after the above loop
            if (i < 0) {
                return true;
                // Shift the pattern so that the next character in text
                // aligns with the last occurrence of it in pattern.
                // The condition (shift + M < N) is necessary for the case when
                // pattern occurs at the end of text */
                // shift += ((shift + M) < N) ? M - bad[text[shift + M]] : 1;
            } else {
                // Shift the pattern so that the bad character in text
                // aligns with the last occurrence of it in pattern. The
                // max function is used to make sure that we get a positive
                // shift. We may get a negative shift if the last occurrence
                // of bad character in pattern is on the right side of the current character.
                shift += Math.max(1, i - bad[text[shift + i]]);
            }
        }
        return false;
    }

    private int[] fillHeuristicChar(char[] pattern) {
        int[] bad = new int[Matcher.CHARACTER_SIZE];

        // Initialize all to -1
        for (int i = 0; i < Matcher.CHARACTER_SIZE; ++i) {
            bad[i] = -1;
        }

        // Fill with the last occurrence of each character in pattern
        for (int i = 0; i < pattern.length; ++i) {
            bad[pattern[i]] = i;
        }
        return bad;
    }
}
