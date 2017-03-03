package de.chirtz.armband.filter.filter_properties;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;


import java.util.regex.Pattern;

import de.chirtz.armband.R;


public class ContentFilterProperty extends AbstractConditionProperty {

    private Switch contentFilterSwitch;
    private EditText matchTitleEditText, matchContentEditText;
    private TextView matchTitleTextView, matchContentTextView;

    public ContentFilterProperty(Context context, ViewGroup rootView, Bundle values) {
        super(context, rootView, values);
    }

    public ContentFilterProperty(Bundle data) {
        super(data);
    }

    @Override
    public String getStateDescription() {
        if (isContentFilterEnabled()) {
            return "content filter";
        }
        return null;
    }

    private void setStringFilterEnabled(boolean enabled) {
        contentFilterSwitch.setChecked(enabled);
        matchTitleEditText.setEnabled(enabled);
        matchContentEditText.setEnabled(enabled);
        matchContentTextView.setEnabled(enabled);
        matchTitleTextView.setEnabled(enabled);
    }

    @Override
    public void initialize(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.filter_property_content_filter, rootView);
        matchTitleEditText = (EditText) v.findViewById(R.id.stringFilterTitleEdit);
        matchContentEditText = (EditText) v.findViewById(R.id.stringFilterContentEdit);
        matchTitleTextView = (TextView) v.findViewById(R.id.matchTitleTextView);
        matchContentTextView = (TextView) v.findViewById(R.id.matchContentTextView);

        contentFilterSwitch = (Switch) v.findViewById(R.id.enableFilterSwitch);

        setStringFilterEnabled(false);
        if (values.containsKey(FILTER_CONDITION_CONTENT_FILTER_ENABLED)) {
            boolean enabled = values.getBoolean(FILTER_CONDITION_CONTENT_FILTER_ENABLED, false);
            setStringFilterEnabled(enabled);
        }
        if (values.containsKey(FILTER_CONDITION_TITLE)) {
            matchTitleEditText.setText(values.getString(FILTER_CONDITION_TITLE));
        }
        if (values.containsKey(FILTER_CONDITION_CONTENT)) {
            matchContentEditText.setText(values.getString(FILTER_CONDITION_CONTENT));
        }
        contentFilterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setStringFilterEnabled(isChecked);
            }
        });
    }

    @Override
    public boolean apply() {
        values.putString(FILTER_CONDITION_CONTENT, matchContentEditText.getText().toString());
        values.putString(FILTER_CONDITION_TITLE, matchTitleEditText.getText().toString());
        values.putBoolean(FILTER_CONDITION_CONTENT_FILTER_ENABLED, contentFilterSwitch.isChecked());
        return true;
    }

    public boolean isContentFilterEnabled() {
        return values.getBoolean(FILTER_CONDITION_CONTENT_FILTER_ENABLED);
    }

    public void setTitleFilterString(String filterString) {
        values.putString(FILTER_CONDITION_TITLE, filterString);
    }

    public void setContentFilterString(String filterString) {
        values.putString(FILTER_CONDITION_CONTENT, filterString);
    }

    private String getTitleFilterString() {
        return values.getString(FILTER_CONDITION_TITLE);
    }

    private String getContentFilterString() {
        return values.getString(FILTER_CONDITION_CONTENT);
    }

    public Pattern getTitleFilterPattern() {
        String filterText = getTitleFilterString();
        if (filterText != null)
            return Pattern.compile(filterText);
        return null;
    }

    public Pattern getContentFilterPattern() {
        String filterText = getContentFilterString();
        if (filterText != null)
            return Pattern.compile(filterText);
        return null;
    }

    public static void serialize(Bundle source, ContentValues target) {
        target.put(FILTER_CONDITION_CONTENT_FILTER_ENABLED, source.getBoolean(FILTER_CONDITION_CONTENT_FILTER_ENABLED) ? 1 : 0);
        target.put(FILTER_CONDITION_TITLE, source.getString(FILTER_CONDITION_TITLE));
        target.put(FILTER_CONDITION_CONTENT, source.getString(FILTER_CONDITION_CONTENT));
    }

    public static void deserialize(Cursor source, Bundle target) {
        target.putBoolean(FILTER_CONDITION_CONTENT_FILTER_ENABLED, source.getInt(source.getColumnIndex(FILTER_CONDITION_CONTENT_FILTER_ENABLED)) != 0);
        target.putString(FILTER_CONDITION_TITLE, source.getString(source.getColumnIndex(FILTER_CONDITION_TITLE)));
        target.putString(FILTER_CONDITION_CONTENT, source.getString(source.getColumnIndex(FILTER_CONDITION_CONTENT)));
    }


}
