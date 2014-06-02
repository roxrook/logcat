package chan.android.app.logcat.search;

import java.util.ArrayList;

public interface Matcher {

    public static final int CHARACTER_SIZE = 256;

    public ArrayList<Integer> match(String text, String pattern);
}
