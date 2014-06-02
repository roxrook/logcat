package chan.android.app.logcat;


final class Logcat {

    final String text;

    final int color;

    public Logcat(String text, int color) {
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public int getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Logcat)) return false;

        Logcat log = (Logcat) o;

        if (!text.equals(log.text)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
