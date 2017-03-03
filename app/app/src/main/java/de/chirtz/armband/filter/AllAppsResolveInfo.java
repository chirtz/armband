package de.chirtz.armband.filter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class AllAppsResolveInfo extends ResolveInfo {

    public final static String CREATOR = "watchconfig";
    public final static String title = "All apps";
    public static final String ALL_APPS = "*";
    private static Drawable drawable;


    public AllAppsResolveInfo(Context context) {
        if (drawable == null)
            //noinspection deprecation
            drawable = context.getResources().getDrawable(android.R.drawable.star_big_on);
        // API 21: replaced by context.getDrawable(android.R.drawable.star_big_on);
    }

    @Override
    public CharSequence loadLabel(@NonNull PackageManager pm) {
        return title;
    }

    public Drawable loadIcon(@NonNull PackageManager pm) {
        return drawable;
    }
}
