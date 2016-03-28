package com.largerlife.counterdemo;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.largerlife.counterdemo.events.BundleWrapper;
import com.largerlife.counterdemo.events.RxBus;
import com.squareup.picasso.Picasso;
import rx.functions.Func1;
import trikita.log.Log;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends BaseFragment {

    public static final String TAG = "HomeFragment";
    public static final String ARG_IMAGE_TO_LOAD = "ImageToLoad";
    public static final @DrawableRes int DEFAULT_STATIC_IMAGE = R.drawable.androiddev_meme;
    @Bind(R.id.imageView) ImageView mImageView;
    @Bind(R.id.counterText) TextView mCounterView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Gallery.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(Bundle bundle) {
        HomeFragment homeFragment = new HomeFragment();
        Log.w("newInstance ", bundle);
        if (bundle != null) {
            homeFragment.setArguments(bundle);
        } else {
            homeFragment.setArguments(new Bundle());
        }
        return homeFragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Loading local image drawable with Picasso
        Picasso picasso = Picasso.with(getActivity());
        @DrawableRes int imageToLoadRes = DEFAULT_STATIC_IMAGE;
        if (savedInstanceState != null) {
            savedInstanceState.getInt(ARG_IMAGE_TO_LOAD, DEFAULT_STATIC_IMAGE);
        }
        picasso.setIndicatorsEnabled(true);
        picasso.load(imageToLoadRes)
               .error(R.drawable.counter_circle)
               .placeholder(R.drawable.counter_circle)
               .centerInside()
               .resizeDimen(R.dimen.home_image_width, R.dimen.home_image_width)
               .into(mImageView);
       /* Uncomment this if you want to download image from an url.
        String urlString = "http://www.thewirelessguy.co
        .uk/wp-content/uploads/2012/11/now-an-android-developer-meme.jpg";
        Uri builtUri = Uri.parse(urlString).buildUpon().build();
        mCounterView.setVisibility(View.VISIBLE);
        new CountingImageDownloader(mCounterView, mImageView) {
            @Override protected void onPostExecute(final Bitmap bitmap) {
                super.onPostExecute(bitmap);
                mCounterView.setVisibility(View.GONE);
            }
        }.execute(builtUri.toString());*/

    }

    @Override public void onResume() {
        super.onResume();
        RxBus.getInstance().send(
              new BundleWrapper(new Bundle())
                    .putString(RxBus.ITEM_NAME, RxBus.FAB)
                    .putString(RxBus.EVENT_TYPE, RxBus.EVT_SHOW_HIDE)
                    .putBoolean(RxBus.PRM_VALUE, Boolean.FALSE)
                    .getBundle()
        );
    }

    @Override public String getFragmentTag() {
        return TAG;
    }

    @Override public String getToolbarTitle() {
        return getString(R.string.main_toolbar_title);
    }

    @Override public void onAppEvent(final Bundle event) {

    }

    @Override public Func1<Bundle, Boolean> getEventFilter() {
        return new Func1<Bundle, Boolean>() {
            @Override public Boolean call(final Bundle bundle) {
                return Boolean.FALSE;
            }
        };
    }
}
