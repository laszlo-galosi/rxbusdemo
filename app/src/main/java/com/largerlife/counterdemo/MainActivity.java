package com.largerlife.counterdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.largerlife.counterdemo.events.BundleWrapper;
import com.largerlife.counterdemo.events.RxBus;
import com.largerlife.counterdemo.events.RxUtils;
import rx.Subscriber;
import rx.android.app.AppObservable;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import trikita.log.Log;

public class MainActivity extends AppCompatActivity
      implements NavigationView.OnNavigationItemSelectedListener,
                 FragmentManager.OnBackStackChangedListener {

    private static final String ARG_CURRENT_FRAGMENT_TAG = "CurrentFragementTag";

    protected CompositeSubscription subscriptions = new CompositeSubscription();
    @Bind(R.id.mainLayout) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.nav_view) NavigationView mNavigationView;
    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.fab) FloatingActionButton mFabButton;
    private Subscriber<Bundle> mEventSubscriber;
    private Bundle mMainBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate", savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        String ttitle = getResources().getString(R.string.main_toolbar_title);
        setSupportActionBar(mToolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        ab.setTitle(ttitle);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        mToolbar.inflateMenu(R.menu.main);
        mToolbar.setTitle(ttitle);
        //Setting up the navigation view.
        mNavigationView.setNavigationItemSelectedListener(this);

        //Creating a main bundle for arguments.
        mMainBundle = new Bundle();
        Bundle extras = getIntent().getExtras();
        //merging with the starting intent's bundle.
        if (extras != null) {
            mMainBundle.putAll(extras);
        }
        //if no saved state storing the default fragment tag, and initializing the activity.
        if (savedInstanceState == null) {
            mMainBundle.putString(ARG_CURRENT_FRAGMENT_TAG, HomeFragment.TAG);
            onRestoreInstanceState(mMainBundle);
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        unsubscribe();
        ButterKnife.unbind(this);
    }

    /**
     * saving all the previously stored argument to a saved instance.
     * @param outState
     */
    @Override protected void onSaveInstanceState(Bundle outState) {
        Log.d("onSaveInstanceState").v(mMainBundle);
        outState.putAll(mMainBundle);
        super.onSaveInstanceState(outState);
    }

    private void unsubscribe() {
        Log.d("unsubscribe:", "hasSubscribers:",
              this.subscriptions != null && this.subscriptions.hasSubscriptions());
        RxUtils.unsubscribeIfNotNull(this.subscriptions);
    }

    /**
     * Restores the previously saved and active fragment from the savedInstanceState bundle.
     * @param savedInstanceState
     */
    @Override protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        Log.d("onRestoreInstanceState").v(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
        String currentFragmentTag =
              savedInstanceState.getString(ARG_CURRENT_FRAGMENT_TAG, HomeFragment.TAG);
        if (currentFragmentTag.equals(HomeFragment.TAG)) {
            addOrReplaceFragment(savedInstanceState, HomeFragment.newInstance(savedInstanceState));
        } else if (currentFragmentTag.equals(CounterFragment.TAG)) {
            addOrReplaceFragment(savedInstanceState,
                                 CounterFragment.newInstance(savedInstanceState));
        }
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        switch (i) {
            case android.R.id.home:
                boolean shouldGoBack = getSupportFragmentManager().getBackStackEntryCount() >= 1;
                if (shouldGoBack) {
                    //getSupportFragmentManager().popBackStack();
                    onBackPressed();
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        boolean shouldGoBack = getSupportFragmentManager().getBackStackEntryCount() >= 1;
        if (shouldGoBack) {
            super.onBackPressed();
        }
    }

    @Override protected void onPause() {
        super.onPause();
        unsubscribe();
    }

    @Override protected void onResume() {
        Log.d("onResume");
        super.onResume();
        this.subscriptions = ensureSubs();
        subscribeActivity(getOrCreateSubscriber(), null);
        onBackStackChanged();
    }

    /**
     * Checks if the activity subscriptions is exist and valid if or had been
     * unsubscribed, creates a new {@link CompositeSubscription}
     *
     * @return the checked subscription object.
     */
    private CompositeSubscription ensureSubs() {
        subscriptions = RxUtils.getNewCompositeSubIfUnsubscribed(this.subscriptions);
        Log.d("ensureSubs:", "hasSubscribers:",
              this.subscriptions != null && this.subscriptions.hasSubscriptions());
        return subscriptions;
    }

    /**
     * Adds th specified {@link RxBus} event subscriber to this activity  {@link CompositeSubscription}
     * @param subscriber {@link RxBus} event subscriber
     * @param filterFunction the {@link RxBus} event filter function
     */
    private void subscribeActivity(Subscriber<? super Bundle> subscriber,
          @Nullable Func1<Bundle, Boolean> filterFunction) {
        Log.d("subscribeActivity: " + this.getClass().getSimpleName(), "filter:", filterFunction);
        if (filterFunction != null) {
            ensureSubs().add(
                  AppObservable.bindActivity(this, RxBus.getInstance().toObservable(filterFunction))
                               .subscribe(subscriber));
        } else {
            ensureSubs().add(
                  AppObservable.bindActivity(this, RxBus.getInstance().toObservable())
                               .subscribe(subscriber));
        }
    }

    /**
     * @return a {@link RxBus} event subscriber lazily.
     */
    private Subscriber<Bundle> getOrCreateSubscriber() {
        final Context thisContext = this;
        if (mEventSubscriber == null || mEventSubscriber.isUnsubscribed()) {
            mEventSubscriber = new Subscriber<Bundle>() {
                @Override public void onCompleted() {
                }

                @Override public void onError(final Throwable e) {
                    SnackbarMaker.getInstance().setMessageRes(R.string.snack_internal_error)
                                 .positiveAction(R.string.snackbar_action_ok)
                                 .make(thisContext, mCoordinatorLayout);
                }

                @Override public void onNext(final Bundle event) {
                    String item = event.getString(RxBus.ITEM_NAME, RxBus.UNKNOWN_ITEM);
                    String eventType = event.getString(RxBus.EVENT_TYPE, RxBus.UNKNOWN_ITEM);
                    if (item.equals(RxBus.SNACKBAR) && eventType.equals(
                          RxBus.EVT_NTERNAL_ERROR)) {
                        Log.d("Received event ", item, "type", eventType).v(event);
                        SnackbarMaker.getInstance()
                                     .setMessage(event.getString(RxBus.PRM_VALUE, getString(
                                           R.string.snack_internal_error)))
                                     .positiveAction(R.string.snackbar_action_ok)
                                     .make(thisContext, mCoordinatorLayout);
                    } else if (item.equals(RxBus.TOOLBAR) && event.containsKey(RxBus.PRM_VALUE)) {
                        Log.d("Received event ", item, "type", eventType).v(event);
                        String title = (String) event.getString(RxBus.PRM_VALUE);
                        mToolbar.setTitle(title);
                    } else if (item.equals(RxBus.FAB) && eventType.equals(RxBus.EVT_SHOW_HIDE)
                          && event.containsKey(RxBus.PRM_VALUE)) {
                        Log.d("Received event ", item, "type", eventType).v(event);
                        boolean visible = event.getBoolean(RxBus.PRM_VALUE, Boolean.TRUE);
                        if (visible) {
                            mFabButton.show();
                        } else {
                            mFabButton.hide();
                        }
                    } else if (item.equals(RxBus.FAB) && eventType.equals(RxBus.EVT_START_STOP)) {
                        Log.d("Received event ", item, "type", eventType).v(event);
                        int state = event.getInt(RxBus.PRM_VALUE);
                        switch (state) {
                            case RxBus.RESTART_COUNTER:
                            case RxBus.RESUME_COUNTER:
                                mFabButton.setImageResource(R.drawable.ic_pause_white_24dp);
                                break;
                            case RxBus.PAUSE_COUNTER:
                            case RxBus.STOP_COUNTER:
                                mFabButton.setImageResource(R.drawable.ic_play_white_24dp);
                                break;
                        }
                        mMainBundle.putInt(CounterFragment.ARG_COUNTER_STATE, state);
                    }
                }
            };
        }
        return mEventSubscriber;
    }

    @Override public void onBackStackChanged() {
        int stackSize = getSupportFragmentManager().getBackStackEntryCount();
        if (stackSize == 0) {
            mToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
            mToolbar.setTitle(R.string.main_toolbar_title);
            return;
        }
        mToolbar.setNavigationIcon(stackSize >= 1 ? R.drawable.ic_arrow_back_white_24dp
                                                  : R.drawable.ic_menu_white_24dp);
        String fragmentTag =
              getSupportFragmentManager().getBackStackEntryAt(stackSize - 1).getName();
        BaseFragment fragment =
              (BaseFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment != null) {
            mToolbar.setTitle(fragment.getToolbarTitle());
        }
    }

    /**
     * Add or replace a fragment to the activity container view, based on it's fragment save.
     * @param savedInstanceState the saved instance of this activity
     * @param fragment to be add or replace.
     * @param <T>
     */
    private <T extends BaseFragment> void addOrReplaceFragment(@Nullable Bundle savedInstanceState,
          @NonNull T fragment) {
        if (fragment == null) {
            throw new NullPointerException("addOrReplaceFragment fragment");
        }
        String fragmentTag = fragment.getFragmentTag();
        T foundFragment = null;
        if (savedInstanceState != null) {
            fragmentTag = savedInstanceState.getString(ARG_CURRENT_FRAGMENT_TAG, fragmentTag);
            Log.d("Restoring fragment", fragmentTag, " from savedState:", savedInstanceState);
            foundFragment = (T) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        }
        mMainBundle.putString(ARG_CURRENT_FRAGMENT_TAG, fragmentTag);
        if (foundFragment == null) {
            Log.d(String.format("Cannot find fragment with tag %s", fragmentTag));
            foundFragment = fragment;
            addFragment(R.id.content_main, foundFragment, fragmentTag);
        } else {
            replaceFragment(R.id.content_main, foundFragment, fragmentTag);
        }
    }

    private void addFragment(int containerViewId, BaseFragment fragment, String fragmentTag) {
        FragmentTransaction ft = getSupportFragmentManager()
              .beginTransaction()
              .add(containerViewId, fragment, fragmentTag)
              .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
              .replace(containerViewId, fragment, fragmentTag);
        if (shouldAddToBackStack(fragment)) {
            ft.addToBackStack(fragment.getFragmentTag());
        }
        ft.commit();
    }

    private void replaceFragment(int containerViewId, final BaseFragment fragment, String fragmentTag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction()
                                                            .setTransition(
                                                                  FragmentTransaction
                                                                        .TRANSIT_FRAGMENT_OPEN)
                                                            .replace(containerViewId, fragment,
                                                                     fragmentTag);
        if (shouldAddToBackStack(fragment)) {
            ft.addToBackStack(fragment.getFragmentTag());
        }
        ft.commit();
    }

    /**
     * Returns if the specified fragment should be added to the back stack.
     * @param fragmentToAdd the new fragment to be added or not
     * @return true, if the previous backstack entry not the fragment to be added.
     */
    private boolean shouldAddToBackStack(final BaseFragment fragmentToAdd) {
        final String fragmentTagToAdd = ((BaseFragment) fragmentToAdd).getFragmentTag();
        final int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        String previousFragmentTag = HomeFragment.TAG;
        if (backStackCount >= 1) {
            previousFragmentTag =
                  getSupportFragmentManager().getBackStackEntryAt(backStackCount - 1).getName();
        }
        return !previousFragmentTag.equals(fragmentTagToAdd);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {

            HomeFragment fragment = HomeFragment.newInstance(getMetaData());
            addOrReplaceFragment(null, fragment);
        } else if (id == R.id.nav_counter) {
            CounterFragment fragment = CounterFragment.newInstance(getMetaData());
            addOrReplaceFragment(null, fragment);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * @return a Bundle containing all the meta data specified in the android manifest file.
     */
    private Bundle getMetaData() {
        if (mMainBundle == null) {
            mMainBundle = new Bundle();
        }
        try {
            ComponentName activityCompName = new ComponentName(this, MainActivity.class);
            Bundle metaData =
                  getPackageManager().getActivityInfo(activityCompName,
                                                      PackageManager.GET_META_DATA).metaData;
            if (metaData != null) {
                mMainBundle.putAll(metaData);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("getMetaData: error", e);
        }
        return mMainBundle;
    }

    /**
     * Fab button clicklistener, sets the idling state of the counter, based on the
     * saved argument.
     */
    @OnClick(R.id.fab)
    public void onFabClicked() {
        int state = mMainBundle.getInt(CounterFragment.ARG_COUNTER_STATE, RxBus.RESTART_COUNTER);
        switch (state) {
            case RxBus.RESTART_COUNTER:
            case RxBus.RESUME_COUNTER:
                state = RxBus.PAUSE_COUNTER;
                break;
            case RxBus.STOP_COUNTER:
                state = RxBus.RESTART_COUNTER;
                break;
            case RxBus.PAUSE_COUNTER:
                state = RxBus.RESUME_COUNTER;
                break;
        }
        RxBus.getInstance().send(
              new BundleWrapper(new Bundle())
                    .putString(RxBus.ITEM_NAME, RxBus.COUNTER)
                    .putString(RxBus.EVENT_TYPE, RxBus.EVT_START_STOP)
                    .putInt(RxBus.PRM_VALUE, state)
                    .getBundle()
        );
    }
}
