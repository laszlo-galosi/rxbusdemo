package com.largerlife.counterdemo.events;

import android.os.Bundle;
import android.support.annotation.Nullable;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import trikita.log.Log;

/**
 * Created by László Gálosi on 27/03/16
 */
public class RxBus {

    public static final String ITEM_NAME = "ItemName";
    public static final String EVENT_TYPE = "EventType";

    public static final String SNACKBAR = "ItemSnackbar";
    public static final String TOOLBAR = "ItemToolbar";

    public static final String UNKNOWN_ITEM = "UnknownItem";
    public static final String UNKNOWN_EVENT = "UnknownEvent";

    public static final String NAVMENU_GALLERY = "Gallery";
    public static final String NAVMENU_COUNTER = "Counter";
    public static final String FAB = "ItemFab";
    public static final String COUNTER = "ItemCounter";
    public static final String EVT_NTERNAL_ERROR = "InternalError";
    public static final String EVT_NAVIGATE = "EventNavigate";
    public static final String EVT_SHOW_HIDE = "EventShowHide";
    public static final String EVT_START_STOP = "EventStartStop";
    public static final String PRM_VALUE = "ParamValue";

    public static final int RESTART_COUNTER = 0;
    public static final int PAUSE_COUNTER = 1;
    public static final int RESUME_COUNTER = 2;
    public static final int STOP_COUNTER = 3;




    // If multiple threads are going to emit events to this
    // then it must be made thread-safe like this instead
    private final Subject<Bundle, Bundle> mBus;

    public static RxBus getInstance() {
        return SInstanceHolder.sInstance;
    }

    public RxBus send(Bundle event) {
        Log.d("sending ", event.getString(ITEM_NAME, "Unknown Item"), "type",
              event.getString(EVENT_TYPE, "Unknown Type")).v(event);
        mBus.onNext(event);
        return this;
    }

    public Observable<Bundle> toObservable() {
        return mBus;
    }

    public boolean hasObservers() {
        return mBus.hasObservers();
    }

    public Observable<Bundle> toObservable(
          @Nullable Func1<Bundle, Boolean> filterFunction) {
        return mBus.filter(filterFunction);
    }

    private static class SInstanceHolder {
        private static final RxBus sInstance = new RxBus();
    }

    private RxBus() {
        this.mBus = new SerializedSubject<Bundle, Bundle>(
              PublishSubject.<Bundle>create());
    }
}
