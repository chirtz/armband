package de.chirtz.armband.filter;


import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;
import com.nhaarman.listviewanimations.util.Swappable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import de.chirtz.armband.MainActivity;
import de.chirtz.armband.R;
import de.chirtz.armband.filter.filter_properties.AppProperty;
import de.chirtz.armband.filter.filter_properties.FilterProperty;

class FilterListAdapter extends ArrayAdapter<Filter> implements Swappable, UndoAdapter {

    private final static String TAG = "FilterListAdapter";
    private final LayoutInflater inflater;
    private final MainActivity activity;
    private final HashMap<Filter, Integer> idMap;
    private int currentID = 0;

    @Override
    public void swapItems(int i, int i1) {
        int min = Math.min(i, i1);
        int max = Math.max(i, i1);
        Filter f1 = getItem(max);
        Filter f2 = getItem(min);
        super.remove(f1);
        super.remove(f2);
        super.insert(f1, min);
        super.insert(f2, max);
    }

    @NonNull
    @Override
    public View getUndoView(int i, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = getUndoView(parent);
        }
        return convertView;
    }

    private View getUndoView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.undo_layout, parent, false);
    }

    @NonNull
    @Override
    public View getUndoClickView(@NonNull View view) {
        return view.findViewById(R.id.undoButton);
    }

    private class ViewHolder {
        TextView titleText;
        TextView messageText;
        CheckBox enabledBox;
        View colorView;
        View handleView;
    }

    @Override
    public void remove(Filter f) {
        try {
            super.remove(f);
            idMap.remove(f);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void remove(int position) {
        try {
            remove(getItem(position));
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void add(Filter f) {
        super.add(f);
        idMap.put(f, currentID++);
    }

    @Override
    public void addAll(@NonNull Collection<? extends Filter> filters) {
        super.addAll(filters);
        for (Filter f: filters)
            idMap.put(f, currentID++);
    }

    @Override
    public void addAll(Filter... items) {
        super.addAll(items);
        for (Filter f: items)
            idMap.put(f, currentID++);
    }

    @Override
    public void insert(Filter f, int pos) {
        super.insert(f, pos);
        idMap.put(f, currentID++);
    }


    public FilterListAdapter(AppCompatActivity parent, List<Filter> items) {
        super(parent, android.R.layout.simple_list_item_1, items);
        inflater = (LayoutInflater) parent.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        activity = (MainActivity) parent;
        idMap = new HashMap<>(items.size());
        for (Filter f: items) {
            idMap.put(f, currentID++);
        }
    }

    @Override
    public long getItemId(int position) {
        int INVALID_ID = -1;
        try {
            Filter item = getItem(position);
            if (item == null) {
                return INVALID_ID;
            }
            Integer i = idMap.get(item);
            if (i == null) {
                return INVALID_ID;
            }
            return i;
        } catch (IndexOutOfBoundsException e) {
            return INVALID_ID;
        }
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        View viewToUse;

        if (convertView == null) {
            viewToUse = inflater.inflate(R.layout.filter_list_item, parent, false);
            holder = new ViewHolder();
            holder.titleText = (TextView) viewToUse.findViewById(R.id.titleTextView);
            holder.messageText = (TextView) viewToUse.findViewById(R.id.messageTextView);
            holder.enabledBox = (CheckBox) viewToUse.findViewById(R.id.enabledCheckBox);
            //holder.positionText = (TextView) viewToUse.findViewById(R.id.positionTextView);
            holder.handleView = viewToUse.findViewById(R.id.handle_view);
            viewToUse.setTag(holder);
        } else {
            viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }

        final Filter f = getItem(position);
        StringBuilder sb = new StringBuilder();
        assert f != null;
        AppProperty appProperty = f.getProperty(FilterProperty.Type.App);
        sb.append(AppChooserDialogFragment.getAppNameForPackage(getContext(), appProperty.getValue()));
        if (f.isSystemFilter()) {
            sb.append(" (System)");
        }
        holder.titleText.setText(sb.toString().trim());
        holder.enabledBox.setChecked(f.isEnabled());
        holder.enabledBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                f.setEnabled(b);
                FilterDatabase db = new FilterDatabase(getContext());
                db.updateEntry(f);
                activity.sendFiltersUpdatedBroadcast();
            }
        });

        if (getCount() > 1) {
            holder.handleView.setVisibility(View.VISIBLE);
            if (position == 0)
                holder.handleView.setBackgroundResource(R.drawable.reorder_down);
            else if (position == getCount()-1)
                holder.handleView.setBackgroundResource(R.drawable.reorder_up);
            else
                holder.handleView.setBackgroundResource(R.drawable.reorder);
        } else {
            holder.handleView.setVisibility(View.GONE);
        }

        holder.messageText.setText(f.getDescription());
        return viewToUse;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}

