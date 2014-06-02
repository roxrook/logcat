package chan.android.app.logcat.search;

import java.util.Arrays;

public class KnuthMorrisPrattMatcher extends AbstractMatcher {

    private int[] dfa;

    public KnuthMorrisPrattMatcher(char[] pattern) {
        super(pattern);
        buildDfa(pattern);
    }

    @Override
    public boolean match(char[] text) {
        final int M = pattern.length;
        final int N = text.length;
        int i = 0;
        int j = 0;
        while (i < N) {
            while (j != -1 && (j == M || pattern[j] != text[i])) {
                j = dfa[j];
            }
            i++;
            j++;
            if (j == M) {
                return true;
            }
        }
        return false;
    }

    private void buildDfa(char[] pattern) {
        final int M = pattern.length;
        dfa = new int[M + 1];
        Arrays.fill(dfa, -1);
        int j;
        for (int i = 1; i <= M; ++i) {
            j = dfa[i - 1];
            while (j != -1 && pattern[j] != pattern[i - 1]) {
                j = dfa[j];
            }
            dfa[i] = j + 1;
        }
    }
}
