package chan.android.app.logcat.search;

public class RabinKarpMatcher extends AbstractMatcher {

    private final int PRIME;

    public RabinKarpMatcher(char[] pattern) {
        super(pattern);
        PRIME = 17;
    }

    public boolean match(char[] text) {
        return match(text, PRIME);
    }

    private boolean match(char[] text, final int prime) {
        final int M = pattern.length;
        final int N = text.length;
        int i, j;

        // Hash for text
        int ht =  0;
        // Hash for pattern
        int hp = 0;
        int h = 1;

        // Value of h should be pow(d, m - 1)
        for (i = 0; i < M - 1; ++i) {
            h = (h * Matcher.CHARACTER_SIZE) % prime;
        }

        // Compute hash value for pattern and first sliding window of text
        for (i = 0; i < M; ++i) {
            hp = (Matcher.CHARACTER_SIZE * hp + pattern[i]) % prime;
            ht = (Matcher.CHARACTER_SIZE * ht + text[i]) % prime;
        }

        // Slide the pattern over text to search
        int windowSize = N - M;
        for (i = 0; i <= windowSize; ++i) {
            // Check the hash value of current window of text & pattern
            // If they're equal then we need only check character one by one
            if (hp == ht) {
                for (j = 0; j < M; ++j) {
                    if (text[i + j] != pattern[j]) {
                        break;
                    }
                }
                if (j == M) {
                    return true;
                }
            }

            // Compute hash value of the next window, remove leading digits and trailing digits
            if (i < windowSize) {
                ht = (Matcher.CHARACTER_SIZE * (ht - text[i] * h) + text[i + M]) % prime;
                if (ht < 0) {
                    ht = ht + prime;
                }
            }
        }
        return false;
    }
}
