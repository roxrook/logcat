package chan.android.app.logcat;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;
import chan.android.app.logcat.search.AbstractMatcher;
import chan.android.app.logcat.search.KnuthMorrisPrattMatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LogItemAdapter extends BaseAdapter {

    private Context appContext;

    private List<Logcat> logs;

    private List<Logcat> copy;

    private Set<Logcat> set;

    private final boolean unique;

    public LogItemAdapter(Context context, boolean unique) {
        this.appContext = context.getApplicationContext();
        this.logs = new ArrayList<Logcat>();
        this.copy = new ArrayList<Logcat>();
        this.set = new HashSet<Logcat>();
        this.unique = unique;
    }

    public void clearLogs() {
        logs.clear();
        set.clear();
        notifyDataSetChanged();
    }

    public void addLog(Logcat log) {
        if (!unique) {
            set.add(log);
            logs.add(log);
            copy.add(log);
        } else {
            if (!set.contains(log)) {
                logs.add(log);
                copy.add(log);
                set.add(log);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return logs.size();
    }

    @Override
    public Object getItem(int position) {
        return logs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder {
        TextView textView;

        public ViewHolder(View v) {
            textView = (TextView) v.findViewById(R.id.log_row_$_textview);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.log_row, parent, false);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        vh.textView.setText(logs.get(position).getText());
        vh.textView.setTextColor(logs.get(position).getColor());
        return convertView;
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                logs = (List<Logcat>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Logcat> filteredResults;
                if (constraint.toString().equals("")) {
                    filteredResults = copy;
                } else {
                    filteredResults = getFilterList(constraint);
                }
                results.values = filteredResults;
                return results;
            }

            private List<Logcat> getFilterList(CharSequence constraint) {
                String criteria = constraint.toString().toLowerCase();
                AbstractMatcher matcher = new KnuthMorrisPrattMatcher(criteria.toCharArray());
                List<Logcat> temp = new ArrayList<Logcat>();
                for (Logcat log : copy) {
                    if (matcher.match(log.getText().toLowerCase().toCharArray())) {
                        temp.add(log);
                    }
                }
                return temp;
            }
        };
    }
}

