/**
 * Interface for classes that can be converted to a Bundle representation
 */
package de.chirtz.armband.common;

import android.os.Bundle;

public abstract class Bundleable {

    protected final static String REQ_ID = "REQ_ID";
    final static String ID = "ID";

    protected final Bundle data;

    protected Bundleable() {
        data = new Bundle();
    }

    protected Bundleable(Bundle b) {
        data = new Bundle();
        data.putAll(b);
    }

    public Bundle getBundle() {
        return data;
    }

}
