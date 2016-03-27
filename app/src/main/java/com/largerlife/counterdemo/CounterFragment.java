package com.largerlife.counterdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.largerlife.counterdemo.events.BundleWrapper;
import com.largerlife.counterdemo.events.RxBus;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import trikita.log.Log;

/**
 * A simple {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link CounterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CounterFragment extends BaseFragment {

    public static final String TAG = "CounterFragment";

    //Arguments to be saved and restored.
    public static final String ARG_COUNTER_STATE = "CounterState";
    public static final String ARG_COUNTER_VALUE = "CounterValue";

    @Bind(R.id.counterText) TextView mCounterView;

    private Subscription mCounterSubscription;

    //The counter object.
    private AtomicLong mCounter = new AtomicLong(0);
    //The counter idling state.
    private int mCounterState;

    /**
     * Use this factory method to create a new instance of
     * this fragment from the provided {@link Bundle}.
     *
     * @return A new instance of fragment Counter.
     */
    public static CounterFragment newInstance(Bundle bundle) {
        CounterFragment fragment = new CounterFragment();
        Log.w("newInstance ", bundle);
        if (bundle != null) {
            fragment.setArguments(bundle);
        } else {
            fragment.setArguments(new Bundle());
        }
        return fragment;
    }

    public CounterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View fragmentView = inflater.inflate(R.layout.fragment_counter, container, false);
        ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override public void onViewStateRestored(@Nullable final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        RxBus.getInstance().send(
              new BundleWrapper(new Bundle())
                    .putString(RxBus.ITEM_NAME, RxBus.FAB)
                    .putString(RxBus.EVENT_TYPE, RxBus.EVT_START_STOP)
                    .putInt(RxBus.PRM_VALUE,
                            getArguments().getInt(RxBus.PRM_VALUE, RxBus.RESTART_COUNTER))
                    .getBundle()
        );
    }

    @Override public void onResume() {
        super.onResume();
        RxBus.getInstance().send(
              new BundleWrapper(new Bundle())
                    .putString(RxBus.ITEM_NAME, RxBus.FAB)
                    .putString(RxBus.EVENT_TYPE, RxBus.EVT_SHOW_HIDE)
                    .putBoolean(RxBus.PRM_VALUE, Boolean.TRUE).getBundle()
        );
        loadStateArguments();
    }

    @Override public void onPause() {
        super.onPause();
        saveStateArguments();
    }

    @Override public Func1<Bundle, Boolean> getEventFilter() {
        return new Func1<Bundle, Boolean>() {
            @Override public Boolean call(final Bundle event) {
                String item = event.getString(RxBus.ITEM_NAME, RxBus.UNKNOWN_ITEM);
                String eventType = event.getString(RxBus.EVENT_TYPE, RxBus.UNKNOWN_EVENT);
                return item.equals(RxBus.COUNTER) && eventType.equals(RxBus.EVT_START_STOP);
            }
        };
    }

    @Override public void onAppEvent(final Bundle event) {
        if (event.containsKey(RxBus.PRM_VALUE)) {
            String item = event.getString(RxBus.ITEM_NAME, RxBus.UNKNOWN_ITEM);
            String eventType = event.getString(RxBus.EVENT_TYPE, RxBus.UNKNOWN_ITEM);
            Log.d("Received event ", item, "type", eventType).v(event);
            handleCounterIdlingState(event.getInt(RxBus.PRM_VALUE));
        }
    }

    @Override public String getFragmentTag() {
        return TAG;
    }

    @Override protected void loadStateArguments() {
        super.loadStateArguments();
        mCounter.set(getArguments().getInt(ARG_COUNTER_VALUE, 0));
        mCounterView.setText(String.format("%d", mCounter.intValue()));
        handleCounterIdlingState(getArguments().getInt(ARG_COUNTER_STATE, RxBus.RESTART_COUNTER));
    }

    @Override protected void saveStateArguments() {
        getArguments().putInt(ARG_COUNTER_STATE, mCounterState);
        getArguments().putInt(ARG_COUNTER_VALUE, Integer.valueOf(mCounter.intValue()));
        super.saveStateArguments();
    }

    @Override public String getToolbarTitle() {
        return getString(R.string.toolbar_title_counter);
    }

    private void handleCounterIdlingState(final int state) {
        mCounterState = state;
        switch (mCounterState) {
            case RxBus.RESTART_COUNTER:
                mCounter.set(0);
                executeCounting();
                break;
            case RxBus.RESUME_COUNTER:
                executeCounting();
                break;
            case RxBus.PAUSE_COUNTER:
            case RxBus.STOP_COUNTER:
                unsubscribe(mCounterSubscription);
                break;
        }
        //Now sending back an event to change fab icon.
        RxBus.getInstance().send(
              new BundleWrapper(new Bundle())
                    .putString(RxBus.ITEM_NAME, RxBus.FAB)
                    .putString(RxBus.EVENT_TYPE, RxBus.EVT_START_STOP)
                    .putInt(RxBus.PRM_VALUE, mCounterState)
                    .getBundle()
        );
    }

    private void executeCounting() {
        if (mCounterSubscription == null || mCounterSubscription.isUnsubscribed()) {
            mCounterSubscription = getCounterObservable()
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Action1<AtomicLong>() {
                      @Override public void call(final AtomicLong atomicLong) {
                          Log.v("Counting", atomicLong);
                          mCounterView.setText(String.format("%d", atomicLong.longValue()));
                      }
                  });
        }
        ensureSubs().add(mCounterSubscription);
    }

    /**
     * Returns an Observable which increments the counter value in every second until the counter
     * is
     * stopped or paused.
     */
    private Observable<AtomicLong> getCounterObservable() {
        return Observable.interval(1, TimeUnit.SECONDS)
                         .flatMap(new Func1<Long, Observable<AtomicLong>>() {
                             @Override public Observable<AtomicLong> call(final Long tick) {
                                 mCounter.incrementAndGet();
                                 return Observable.just(mCounter);
                             }
                         });
    }
}
