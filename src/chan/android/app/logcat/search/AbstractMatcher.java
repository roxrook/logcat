package chan.android.app.logcat.search;


public abstract class AbstractMatcher {

    protected final char[] pattern;

    protected AbstractMatcher(char[] pattern) {
        this.pattern = pattern;
    }

    public abstract boolean match(char[] text);
}
