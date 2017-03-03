package de.chirtz.armband.filter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.chirtz.armband.R;

class AppAdapter extends ArrayAdapter<ResolveInfo> {

    private final PackageManager pm;
    private final LayoutInflater inflater;
    private static final Rect bounds = new Rect(0, 0, 85, 85);

    AppAdapter(Context context, PackageManager pm, List<ResolveInfo> apps) {
        super(context, R.layout.text_row, apps);
        this.inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        this.pm = pm;
    }

    private class ViewHolder {
        TextView labelView;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        View viewToUse;
        if (convertView == null) {
            viewToUse = inflater.inflate(R.layout.text_row, parent, false);
            holder = new ViewHolder();
            holder.labelView = (TextView) viewToUse.findViewById(R.id.label);
            viewToUse.setTag(holder);
        } else {
            viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }
        ResolveInfo info = getItem(position);
        assert info != null;
        holder.labelView.setText(info.loadLabel(pm));
        Drawable appIcon = info.loadIcon(pm);
        appIcon.setBounds(bounds);
        holder.labelView.setCompoundDrawables(appIcon, null, null, null);
        return viewToUse;
    }

}