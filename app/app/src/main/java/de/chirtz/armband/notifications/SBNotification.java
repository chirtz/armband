package de.chirtz.armband.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.service.notification.StatusBarNotification;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.chirtz.armband.common.BidiLinkedHashMap;
import de.chirtz.armband.common.Bundleable;
import de.chirtz.armband.R;
import de.chirtz.armband.filter.AllAppsResolveInfo;
import de.chirtz.armband.filter.Filter;
import de.chirtz.armband.filter.filter_properties.AccumulateTextProperty;
import de.chirtz.armband.filter.filter_properties.AppProperty;
import de.chirtz.armband.filter.filter_properties.ContentFilterProperty;
import de.chirtz.armband.filter.filter_properties.FilterProperty;
import de.chirtz.armband.filter.filter_properties.MinPriorityProperty;
import de.chirtz.armband.filter.filter_properties.PeopleProperty;

public class SBNotification extends Bundleable {

    private static final String APP = "APP_NAME";
    private static final String PKG = "PKG";
    private static final String TIME = "TIME";
    private static final String ID = "ID";
    private static final String PRIORITY = "PRIORITY";
    private static final String ICON = "ICON";
    private static final String PEOPLE = "PEOPLE";
    private static final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    public static final BidiLinkedHashMap<Integer, String> priorityMap = new BidiLinkedHashMap<>(5);

    static {
        priorityMap.put(Notification.PRIORITY_MIN, "Minimum");
        priorityMap.put(Notification.PRIORITY_LOW, "Low");
        priorityMap.put(Notification.PRIORITY_DEFAULT, "Default");
        priorityMap.put(Notification.PRIORITY_HIGH, "High");
        priorityMap.put(Notification.PRIORITY_MAX, "Maximum");
    }


    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private SBNotification(String appName, String pkg, long time, int id, int priority, Bundle extras, Drawable icon) {
        this(appName, pkg, fmt.format(new Date(time)), id, priority, extras, icon);
    }

    private SBNotification(String appName, String pkg, String time, int id, int priority, Bundle extras, Drawable icon) {
        super();
        data.putString(APP, appName);
        data.putString(PKG, pkg);
        data.putString(TIME, time);
        data.putInt(ID, id);
        data.putInt(PRIORITY, priority);
        putString(Notification.EXTRA_TITLE, extras.get(Notification.EXTRA_TITLE));
        putString(Notification.EXTRA_TEXT, extras.get(Notification.EXTRA_TEXT));
        if (extras.containsKey(Notification.EXTRA_PEOPLE)) {
            data.putStringArray(PEOPLE, extras.getStringArray(Notification.EXTRA_PEOPLE));
        }
        data.putParcelable(ICON, drawableToBitmap(icon));
    }

    public Bitmap getIcon() {
        return data.getParcelable(ICON);
    }

    public LinkedList<String> getPeopleNames(Context context) {
        String[] people = data.getStringArray(PEOPLE);
        if (people == null) return null;
        LinkedList<String> names = new LinkedList<>();
        for (String person : people) {
            Cursor c = context.getContentResolver().query(Uri.parse(person), null, null, null, null);
            assert c != null;
            while (c.moveToNext()) {
                names.add(c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
            }
            c.close();
        }
        return names;
    }

    private LinkedList<String> getPeopleKeys(Notification n, Context context) {
        LinkedList<String> keys = new LinkedList<>();
        if (!n.extras.containsKey(Notification.EXTRA_PEOPLE)) return keys;
        for (String person : n.extras.getStringArray(Notification.EXTRA_PEOPLE)) {
            Cursor c = context.getContentResolver().query(Uri.parse(person), null, null, null, null);
            assert c != null;
            while (c.moveToNext()) {
                String key = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                keys.add(key);
            }
            c.close();
        }
        return keys;
    }

    public void update(StatusBarNotification sbn, boolean appendText) {
        data.putString(TIME, fmt.format(sbn.getPostTime()));
        data.putInt(PRIORITY, sbn.getNotification().priority);
        Object str = sbn.getNotification().extras.get(Notification.EXTRA_TEXT);
        if (str == null) return;
        String text = String.valueOf(str).trim();
        if (appendText)
            data.putString(Notification.EXTRA_TEXT, data.getString(Notification.EXTRA_TEXT) + " / " + text);
        else
            data.putString(Notification.EXTRA_TEXT, text);
    }


    private void putString(String key, Object value) {
        if (value == null)
            data.putString(key, null);
        else
            data.putString(key, String.valueOf(value).trim());
    }



    @TargetApi(23)
    public SBNotification(String appName, StatusBarNotification sbn, int id, Context context) {
        this(
                appName,
                sbn.getPackageName(),
                sbn.getPostTime(),
                id,
                sbn.getNotification().priority,
                sbn.getNotification().extras,
                (Build.VERSION.SDK_INT >= 23) ? sbn.getNotification().getSmallIcon().loadDrawable(context) : context.getDrawable(R.drawable.empty));
    }

    public static int hashCode(StatusBarNotification sbn) {
        return sbn.getPackageName().hashCode() * 1000 + String.valueOf(sbn.getTag()).hashCode() * 100 + sbn.getId();
        //int hash =  String.valueOf(sbn.getKey()).hashCode() + String.valueOf(sbn.getTag()).hashCode() *1000 + sbn.getId()*100 + String.valueOf(sbn.getKey()).hashCode()*10;
        //return hash;
    }

    public String getAppName() {
        return data.getString(APP);
    }

    public String getAppPackage() {
        return data.getString(PKG);
    }

    public String getText() {
        String text = data.getString(Notification.EXTRA_TEXT);
        if (text == null)
            return "";
        return text;
    }

    public String getTitle() {
        String title = data.getString(Notification.EXTRA_TITLE);
        if (title == null)
            return "";
        return title;
    }

    public int getPriority() {
        return data.getInt(PRIORITY);
    }

    public static String getPriorityString(int priority) {
        return priorityMap.get(priority);
    }

    public Filter getFirstMatchingFilter(List<Filter> filterList, StatusBarNotification sbn, Context context) {
        Notification n = sbn.getNotification();
        for (Filter f : filterList) {
            if (!f.isEnabled()) continue;
            AppProperty appProperty = f.getProperty(FilterProperty.Type.App);
            if (!(appProperty.getValue().equals(sbn.getPackageName()) || appProperty.getValue().equals(AllAppsResolveInfo.ALL_APPS)))
                continue;
            MinPriorityProperty minPriorityProperty = f.getProperty(FilterProperty.Type.MinPriority);
            if (minPriorityProperty.getValue() > n.priority)
                continue; // filter too low priorities
            PeopleProperty peopleProperty = f.getProperty(FilterProperty.Type.People);
            String peopleKey = peopleProperty.getKey();
            if (peopleKey != null) {
                boolean foundPerson = false;
                for (String person : getPeopleKeys(n, context)) {
                    if (person.equals(peopleKey)) {
                        foundPerson = true;
                        break;
                    }
                }
                if (!foundPerson) {
                    continue;
                }
            }
            ContentFilterProperty contentFilterProperty = f.getProperty(FilterProperty.Type.ContentFilter);
            if ((!contentFilterProperty.isContentFilterEnabled())) {
                return f;
            } else {
                AccumulateTextProperty accumulateTextProperty = f.getProperty(FilterProperty.Type.AccumulateText);
                String titleMatch = n.extras.getString(Notification.EXTRA_TITLE);
                String contentMatch;
                if (accumulateTextProperty.getValue())
                    contentMatch = getText() + "\n" + String.valueOf(n.extras.getString(Notification.EXTRA_TEXT));
                else
                    contentMatch = String.valueOf(n.extras.getString(Notification.EXTRA_TEXT));

                if (!(titleMatch == null || titleMatch.equals(""))) {
                    if (!contentFilterProperty.getTitleFilterPattern().matcher(titleMatch).find())
                        continue;
                }

                if (!(contentMatch == null || contentMatch.equals(""))) {
                    if (!contentFilterProperty.getContentFilterPattern().matcher(contentMatch).find())
                        continue;
                }
                return f;
            }
        }
        return null;
    }

    public int getId() {
        return hashCode();
    }


    @Override
    public int hashCode() {
        return data.getInt(ID);
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof SBNotification && this.hashCode() == obj.hashCode();
    }


}
