package com.largerlife.counterdemo;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by László Gálosi on 17/03/16
 */
public class SnackbarMaker {

    @StringRes int messageRes = 0;
    @Nullable String message;
    @Nullable
    @StringRes int positiveActionRes = 0;
    @StringRes int negativeActionRes = 0;
    @StringRes int titleRes = 0;
    @Nullable
    @ColorRes
    int actionColorRes = R.color.colorPrimary;
    @Nullable View.OnClickListener positiveActionClickListener;
    @Nullable View.OnClickListener negativeActionClickListener;
    int duration = Snackbar.LENGTH_LONG;

    private View.OnClickListener emptyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //empty click listemer for dismissing snackbar
        }
    };

    public static SnackbarMaker getInstance() {
        return SInstanceHolder.sInstance;
    }

    public SnackbarMaker positiveAction(@StringRes final int actionRes) {
        this.positiveActionRes = actionRes;
        return this;
    }

    public SnackbarMaker negativeAction(@StringRes final int actionRes) {
        this.negativeActionRes = actionRes;
        return this;
    }

    public SnackbarMaker title(@StringRes final int titleRes) {
        this.titleRes = titleRes;
        return this;
    }

    public SnackbarMaker positiveActionClicked(
          @Nullable final View.OnClickListener clickListener) {
        this.positiveActionClickListener = clickListener;
        return this;
    }

    public SnackbarMaker negativeActionClicked(
          @Nullable final View.OnClickListener clickListener) {
        this.negativeActionClickListener = clickListener;
        return this;
    }

    public void make(Context context, View parentView) {
        Snackbar snackbar;
        if (messageRes > 0) {
            snackbar = Snackbar.make(parentView, context.getString(messageRes), duration);
        } else {
            snackbar = Snackbar.make(parentView, message, duration);
        }
        if (positiveActionRes != 0) {
            snackbar.setAction(positiveActionRes, positiveActionClickListener == null
                                                  ? emptyClickListener
                                                  : positiveActionClickListener);
        }
        if (actionColorRes != 0) {
            @ColorInt int actionColor = context.getResources().getColor(actionColorRes);
            snackbar.setActionTextColor(actionColor);
        }
        snackbar.show();
    }

    public SnackbarMaker setMessageRes(final int messageRes) {
        this.message = null;
        this.messageRes = messageRes;
        return this;
    }

    public SnackbarMaker setMessage(@Nullable final String message) {
        reset();
        this.message = message;
        return this;
    }

    private void reset() {
        this.messageRes = 0;
        this.message = null;
        this.titleRes = 0;
        this.positiveActionRes = R.string.snackbar_action_ok;
        this.negativeActionRes = 0;
        this.positiveActionClickListener = null;
        this.negativeActionClickListener = null;
    }

    public SnackbarMaker setActionColorRes(@Nullable final int actionColorRes) {
        this.actionColorRes = actionColorRes;
        return this;
    }

    public SnackbarMaker setDuration(final int duration) {
        this.duration = duration;
        return this;
    }

    private static class SInstanceHolder {
        private static final SnackbarMaker sInstance = new SnackbarMaker();
    }

    private SnackbarMaker() {
    }
}

