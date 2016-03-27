package com.largerlife.counterdemo;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.largerlife.counterdemo.events.BundleWrapper;
import com.largerlife.counterdemo.events.RxBus;
import com.largerlife.counterdemo.events.RxUtils;
import rx.Subscriber;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import trikita.log.Log;

/**
 * Created by László Gálosi on 27/03/16
 */
public abstract class BaseFragment extends Fragment {

    protected Subscriber<Bundle> mEventSubscriber;
    protected Func1<Bundle, Boolean> mEventFilter;
    protected CompositeSubscription subscription;

    @CallSuper
    @Override public void onCreate(Bundle savedInstanceState) {
        Log.v(getFragmentTag(), "onCreate")
           .v("savedState:", savedInstanceState);
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @CallSuper @Nullable
    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
          final Bundle savedInstanceState) {
        Log.v(getFragmentTag(), "onCreateView")
           .v("savedState:", savedInstanceState);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @CallSuper
    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        Log.v(getFragmentTag(), "onViewCreated")
           .v("savedState:", savedInstanceState);
        super.onViewCreated(view, savedInstanceState);
    }

    @CallSuper
    @Override public void onActivityCreated(final Bundle savedInstanceState) {
        Log.v(getFragmentTag(), "onActivityCreated")
           .v("savedState:", savedInstanceState)
           .v("arguments:", getArguments());
        super.onActivityCreated(savedInstanceState);
    }

    @CallSuper
    @Override public void onViewStateRestored(@Nullable final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.v(getFragmentTag(), "onViewStateRestored")
           .v("outState", savedInstanceState).v("arguments", getArguments());
    }

    @CallSuper
    @Override public void onResume() {
        Log.d(getFragmentTag(), "onResume");
        resubscribe();
        super.onResume();
    }

    @CallSuper
    @Override public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(getFragmentTag(), "onSaveInstanceState")
           .v("outState", outState).v("arguments", getArguments());
    }

    @CallSuper
    @Override public void onPause() {
        Log.d(getFragmentTag(), "onPause");
        unsubscribe();
        super.onPause();
    }

    /**
     * Unsubscribes all subscribtions from this fragment {@link CompositeSubscription}.
     */
    protected void unsubscribe() {
        Log.v(getFragmentTag(), "unsubscribe");
        unsubscribe(this.subscription);
    }

    /**
     * Unsubscribes the specified {@link Subscription} from the {@link RxBus}.
     * @param sub
     */
    protected void unsubscribe(Subscription sub) {
        //subscription.remove(sub);
        RxUtils.unsubscribeIfNotNull(sub);
    }

    /**
     * Resubscribing to {@link RxBus} events.
     */
    protected void resubscribe() {
        Log.v(getFragmentTag(), "resubscribe");
        ensureSubs().add(AppObservable.bindFragment(this, RxBus.getInstance()
                                                               .toObservable(getEventFilter()))
                                      .subscribe(getOrCreateEventSubscriber()));
    }

    /**
     * @return a new {@link CompositeSubscription} object if not existing, or had been unsubscribed.
     */
    public CompositeSubscription ensureSubs() {
        subscription = RxUtils.getNewCompositeSubIfUnsubscribed(this.subscription);
        return subscription;
    }

    /**
     * Lazily creates an {@link RxBus} event filter
     * Override this in extended fragment.
     *
     * @return a filter function to filter {@link RxBus} events.
     */
    public Func1<Bundle, Boolean> getEventFilter() {
        if (mEventFilter == null) {
            mEventFilter = new Func1<Bundle, Boolean>() {
                @Override public Boolean call(final Bundle appActionItem) {
                    return Boolean.TRUE;
                }
            };
        }
        return mEventFilter;
    }

    /**
     * Lazily created an {@link RxBus} event subscriber for this fragfment.
     *
     * @return an {@link RxBus} event subscriber.
     */
    private Subscriber<Bundle> getOrCreateEventSubscriber() {
        if (mEventSubscriber == null || mEventSubscriber.isUnsubscribed()) {
            mEventSubscriber = new Subscriber<Bundle>() {
                @Override public void onCompleted() {
                }

                @Override public void onError(final Throwable e) {
                    onAppEventError((Exception) e);
                }

                @Override public void onNext(final Bundle event) {
                    onAppEvent(event);
                }
            };
        }
        return mEventSubscriber;
    }

    /**
     * Override this function for custom {@link RxBus} error handling.
     */
    protected void onAppEventError(final Throwable throwable) {
        RxBus.getInstance().send(
              new BundleWrapper(new Bundle())
                    .putString(RxBus.ITEM_NAME, RxBus.SNACKBAR)
                    .putString(RxBus.EVENT_TYPE, RxBus.EVT_NTERNAL_ERROR)
                    .getBundle()
        );
    }

    /**
     * {@link RxBus} event handling.
     */
    public abstract void onAppEvent(Bundle event);

    /**
     * @return the custom fragment tag of the implementing {@link BaseFragment}
     */
    public abstract String getFragmentTag();

    /**
     * Loads all the previously saved argument of this fragment.
     */
    protected void loadStateArguments() {
        Log.v(getFragmentTag(), "loadStateArguments").v(getArguments());
    }

    /**
     * Stores all important argument to restore from a savedInsatanceState {@link Bundle} later.
     */
    protected void saveStateArguments() {
        Log.v(getFragmentTag(), "saveStateArguments").v(getArguments());
    }

    /**
     * @return te custom toolbar title string of the implementing {@link BaseFragment}.
     */
    public abstract String getToolbarTitle();
}
