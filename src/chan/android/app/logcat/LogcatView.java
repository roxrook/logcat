package chan.android.app.logcat;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogcatView extends LinearLayout {

    static final String[] LEVELS = new String[]{
            "<all>",
            "Verbose",
            "Debug",
            "Info",
            "Warn",
            "Error"
    };

    static class LogColor {
        public static final int RED = Color.parseColor("#9d1826");
        public static final int BLUE = Color.parseColor("#000080");
        public static final int ORANGE = Color.parseColor("#FFA500");
        public static final int GREEN = Color.parseColor("#007f00");
        public static final int PURPLE = Color.parseColor("#A500FF");
        public static final int BLACK = Color.parseColor("#000000");
    }

    private int debugColor;
    private int errorColor;
    private int warningColor;
    private int infoColor;
    private int verboseColor;

    private EditText searchView;
    private Spinner levelSpinner;
    private ListView logListView;
    private LogItemAdapter logAdapter;
    private LogcatTask logTask;
    private boolean uniqueLog;

    public LogcatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LogcatView, 0, 0);
        try {
            debugColor = a.getColor(R.styleable.LogcatView_color_debug, LogColor.BLUE);
            errorColor = a.getColor(R.styleable.LogcatView_color_debug, LogColor.RED);
            warningColor = a.getColor(R.styleable.LogcatView_color_debug, LogColor.ORANGE);
            infoColor = a.getColor(R.styleable.LogcatView_color_debug, LogColor.GREEN);
            verboseColor = a.getColor(R.styleable.LogcatView_color_debug, LogColor.BLACK);
            uniqueLog = a.getBoolean(R.styleable.LogcatView_unique_log, true);
        } finally {
            a.recycle();
        }

        // Inflate layout
        View.inflate(context, R.layout.logcat, this);

        // Create a unique async task
        logTask = new LogcatTask(this);

        // Prepare list view
        logListView = (ListView) findViewById(R.id.logcat_view_$_listview_log);
        logAdapter = new LogItemAdapter(context, uniqueLog);
        logListView.setAdapter(logAdapter);

        searchView = (EditText) findViewById(R.id.logcat_view_$_edittext_search);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                logAdapter.getFilter().filter(s.toString());
            }
        });

        levelSpinner = (Spinner) findViewById(R.id.logcat_view_$_spinner_level);
        levelSpinner.setAdapter(new LevelItemAdapter(context, LEVELS));
        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                logAdapter.clearLogs();
                if (LEVELS[position].equals("<all>")) {
                    logTask.setLoggableCondition(new AllCondition());
                } else {
                    logTask.setLoggableCondition(new SingleModeCondition(LEVELS[position].charAt(0)));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ignore
            }
        });
    }

    public void log() {
        if (!logTask.isRunning()) {
            logTask.execute();
        }
    }

    private Logcat createLog(String line) {
        return new Logcat(line, getColor(line.charAt(0)));
    }

    public int getColor(char c) {
        if (c == 'E') {
            return errorColor;
        } else if (c == 'W') {
            return warningColor;
        } else if (c == 'I') {
            return infoColor;
        } else if (c == 'D') {
            return debugColor;
        } else if (c == 'V') {
            return verboseColor;
        }
        return Color.BLACK;
    }

    private void addLog(String line) {
        logAdapter.addLog(createLog(line));
    }

    static class ViewHolder {
        TextView textView;

        public ViewHolder(View v) {
            textView = (TextView) v.findViewById(R.id.level_row_$_textview);
        }
    }

    private static class LevelItemAdapter extends BaseAdapter {

        final Context context;
        final String[] items;

        public LevelItemAdapter(Context context, String[] items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public String getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.level_row, parent, false);
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            vh.textView.setText(items[position]);
            return convertView;
        }
    }

    static class LogcatTask extends AsyncTask<Void, String, Void> {

        static final int BUFFER_SIZE = 2 * 4096;

        volatile boolean running;
        Process process = null;
        Condition loggable;
        LogcatView view;

        public LogcatTask(LogcatView view) {
            this.view = view;
            this.loggable = new AllCondition();
            this.running = false;
        }

        public void setLoggableCondition(Condition condition) {
            loggable = condition;
        }

        public boolean isRunning() {
            return running;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String line = values[0];
            if (loggable.satisfy(line)) {
                view.addLog(line);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            running = true;
            try {
                process = Runtime.getRuntime().exec("logcat");
            } catch (IOException e) {
                running = false;
            }

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()), BUFFER_SIZE);
            } catch (IllegalArgumentException e) {
                running = false;
            }
            try {
                String[] lines = new String[1];
                while (running) {
                    lines[0] = reader.readLine();
                    publishProgress(lines);
                }
            } catch (IOException e) {
                running = false;
            }
            return null;
        }
    }

    interface Condition {
        boolean satisfy(String line);
    }

    static class AllCondition implements Condition {

        @Override
        public boolean satisfy(String line) {
            return (line != null && !line.isEmpty());
        }
    }

    static class SingleModeCondition implements Condition {

        final char mode;

        public SingleModeCondition(char mode) {
            this.mode = mode;
        }

        @Override
        public boolean satisfy(String line) {
            if (line != null && !line.isEmpty()) {
                return line.charAt(0) == mode;
            }
            return false;
        }
    }
}
