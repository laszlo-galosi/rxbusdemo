package com.largerlife.counterdemo.events;

import android.os.Bundle;

/**
 * Created by László Gálosi on 27/03/16
 */
public class BundleWrapper {

    final Bundle mBundle;

    public BundleWrapper(final Bundle bundle) {
        mBundle = bundle;
    }

    public BundleWrapper putString(String key, String value) {
        mBundle.putString(key, value);
        return this;
    }

    public BundleWrapper putInt(String key, int value) {
        mBundle.putInt(key, Integer.valueOf(value));
        return this;
    }

    public BundleWrapper putLong(String key, long value) {
        mBundle.putLong(key, Long.valueOf(value));
        return this;
    }

    public BundleWrapper putAll(final Bundle bundle) {
        mBundle.putAll(bundle);
        return this;
    }

    public BundleWrapper putBoolean(String key, final boolean b) {
        mBundle.putBoolean(key, b);
        return this;
    }

    public Bundle getBundle() {
        return mBundle;
    }
}
