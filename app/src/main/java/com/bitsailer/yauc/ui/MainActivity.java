package com.bitsailer.yauc.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bitsailer.yauc.Preferences;
import com.bitsailer.yauc.R;
import com.bitsailer.yauc.Util;
import com.bitsailer.yauc.YaucApplication;
import com.bitsailer.yauc.event.NetworkErrorEvent;
import com.bitsailer.yauc.event.UserLoadedEvent;
import com.bitsailer.yauc.sync.PhotoManagement;
import com.bitsailer.yauc.sync.SyncAdapter;
import com.bitsailer.yauc.widget.NewPhotosWidgetIntentService;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bitsailer.yauc.Util.AppStart.FIRST_TIME;
import static com.bitsailer.yauc.widget.NewPhotosWidget.EXTRA_NUM_PHOTOS;

/**
 * App entry point and holder of three {@link PhotoListFragment} to
 * display new, favorite and own photos arranged in tabs.
 */
@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity implements
        PhotoListFragment.ClickCallback, PhotoListFragment.LoginCallback,
        SimpleDialogFragment.PositiveClickListener {

    private static final String STATE_TAB_POSITION = "state_tab_position";
    private static final int RC_LOGIN_ACTIVITY = 4711;
    private static final int DIALOG_LOGIN = 0;
    private static final String TAG_DIALOG_LOGIN = "tag_dialog_login";

    private static Preferences mPreferences;
    private int mTabPosition = SectionsPagerAdapter.POSITION_NEW;
    private Tracker mTracker;

    @BindView(R.id.main_content) CoordinatorLayout mMainContent;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPreferences = Preferences.get(this);

        // initialize synchronisation
        SyncAdapter.CreateSyncAccount(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        if (savedInstanceState != null) {
            mTabPosition = savedInstanceState
                    .getInt(STATE_TAB_POSITION, SectionsPagerAdapter.POSITION_NEW);
        }

        if (FIRST_TIME == Util.checkAppStart(this, mPreferences)
                && !mPreferences.isAuthenticated()) {
            welcome();
        }

        // todo: enable this after app approval (without hourly rate limit)
        // update users photos on to keep things synchronized
        /* if (savedInstanceState == null && mPreferences.isAuthenticated()) {
            PhotoManagement.updateUsersPhotos(MainActivity.this,
                    mPreferences.getUserUsername());
        } */

        // reset widget
        Intent intentService = new Intent(this, NewPhotosWidgetIntentService.class);
        intentService.putExtra(EXTRA_NUM_PHOTOS, 0);
        this.startService(intentService);

        // setup icons
        initTabs(mTabPosition);

        // Obtain the shared Tracker instance.
        YaucApplication application = (YaucApplication) getApplication();
        mTracker = application.getDefaultTracker();
        trackScreen();
    }

    private void welcome() {

        SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
                DIALOG_LOGIN,
                getString(R.string.dialog_welcome_title),
                getString(R.string.dialog_welcome_message),
                getString(R.string.button_dialog_dismiss),
                getString(R.string.button_dialog_positive)
        );
        dialogFragment.show(getSupportFragmentManager(), TAG_DIALOG_LOGIN);
    }

    private void arrange(TabLayout.Tab tab) {
        if (tab.getPosition() == SectionsPagerAdapter.POSITION_NEW) {
            if (tab.isSelected()) {
                tab.setIcon(R.drawable.ic_new_releases_accent);
            } else {
                tab.setIcon(R.drawable.ic_new_releases);
            }
        } else if (tab.getPosition() == SectionsPagerAdapter.POSITION_FAVORITES) {
            if (tab.isSelected()) {
                tab.setIcon(R.drawable.ic_favorite_accent);
            } else {
                tab.setIcon(R.drawable.ic_favorite);
            }
        } else if (tab.getPosition() == SectionsPagerAdapter.POSITION_OWN) {
            if (tab.isSelected()) {
                tab.setIcon(R.drawable.ic_account_accent);
            } else {
                tab.setIcon(R.drawable.ic_account);
            }
        }
    }

    private void initTabs(int position) {
        mViewPager.setCurrentItem(position);
        for (int i=0; i < SectionsPagerAdapter.PAGES; i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab != null) {
                arrange(tab);
                switch (i) {
                    case SectionsPagerAdapter.POSITION_NEW:
                        tab.setContentDescription(R.string.content_description_tab_new);
                        break;
                    case SectionsPagerAdapter.POSITION_FAVORITES:
                        tab.setContentDescription(R.string.content_description_tab_favorite);
                        break;
                    case SectionsPagerAdapter.POSITION_OWN:
                        tab.setContentDescription(R.string.content_description_tab_own);
                        break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // change menu item if authorized or not
        if (mPreferences.isAuthenticated()) {
            menu.findItem(R.id.action_auth).setTitle(R.string.action_logout);
        } else {
            menu.findItem(R.id.action_auth).setTitle(R.string.action_login);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_auth) {
            if (mPreferences.isAuthenticated()) {
                // ask for commit to logout
                Snackbar.make(mMainContent, R.string.message_ask_logout, Snackbar.LENGTH_LONG)
                        .setAction(R.string.button_yes, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // logout user: destroy token
                                PhotoManagement
                                        .cleanupUsersPhotos(MainActivity.this, mPreferences.getUserUsername());
                                mPreferences.destroyAuthorization();
                                Snackbar.make(mMainContent, R.string.message_logout, Snackbar.LENGTH_SHORT).show();
                            }
                        }).show();
            } else {
                startLogin();
            }
            return true;
        }
        if (id == R.id.action_refresh) {
            SyncAdapter.syncNow(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_TAB_POSITION, mTabPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        mTabLayout.setOnTabSelectedListener(null);
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @SuppressWarnings("UnusedParameters")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkError(NetworkErrorEvent event) {
        Snackbar.make(mMainContent, R.string.message_network_failed, Snackbar.LENGTH_LONG).show();
    }

    @SuppressWarnings("UnusedParameters")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserLoaded(UserLoadedEvent event) {
        Snackbar.make(mMainContent,
                String.format(getString(R.string.message_login), mPreferences.getUserName()),
                Snackbar.LENGTH_LONG).show();
        // add user id to analytics
        if (!TextUtils.isEmpty(mPreferences.getUserId())) {
            mTracker.set("&uid", mPreferences.getUserId());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_LOGIN_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                PhotoManagement.getUser(this);
            } else {
                Snackbar.make(mMainContent, R.string.message_failure_login, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void startLogin() {
        startActivityForResult(new Intent(this, LoginActivity.class), RC_LOGIN_ACTIVITY);
    }

    /**
     * Greet user if there's an intent from login.
     */
    @Override
    protected void onResume() {
        super.onResume();

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTabPosition = tab.getPosition();
                initTabs(mTabPosition);
                trackScreen();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == SectionsPagerAdapter.POSITION_NEW) {
                    tab.setIcon(R.drawable.ic_new_releases);
                } else if (tab.getPosition() == SectionsPagerAdapter.POSITION_FAVORITES) {
                    tab.setIcon(R.drawable.ic_favorite);
                } else if (tab.getPosition() == SectionsPagerAdapter.POSITION_OWN) {
                    tab.setIcon(R.drawable.ic_account);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Logger.d(tab.getPosition());
            }
        });
    }

    /**
     * Analytics track screen name
     */
    private void trackScreen() {
        String name = String.format("Tab%s", getTabName(mTabPosition));
        mTracker.setScreenName(name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private String getTabName(int tabPosition) {
        if (tabPosition == SectionsPagerAdapter.POSITION_FAVORITES) {
            return getString(R.string.ga_name_tab_favorite);
        } else if (tabPosition == SectionsPagerAdapter.POSITION_OWN) {
            return getString(R.string.ga_name_tab_own);
        } else {
            return getString(R.string.ga_name_tab_new);
        }
    }

    @Override
    public void onItemSelected(Uri uri, PhotoListAdapter.PhotoListItemViewHolder vh) {
        Intent intent = new Intent(this, DetailActivity.class)
                .setData(uri);
        //noinspection unchecked
        Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle();
        startActivity(intent, bundle);
    }

    @Override
    public void onSignInSelected() {
        startLogin();
    }

    @Override
    public void onDialogPositiveClick(int id) {
        if (id == DIALOG_LOGIN) {
            // User clicked Sign in button
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        static final int PAGES = 3;
        static final int POSITION_NEW = 0;
        static final int POSITION_FAVORITES = 1;
        static final int POSITION_OWN = 2;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == POSITION_NEW) {
                return PhotoListFragment.newInstance(PhotoListFragment.PHOTO_TYPE_NEW);
            } else if (position == POSITION_FAVORITES) {
                return PhotoListFragment.newInstance(PhotoListFragment.PHOTO_TYPE_FAVORITES);
            } else if (position == POSITION_OWN) {
                return PhotoListFragment.newInstance(PhotoListFragment.PHOTO_TYPE_OWN);
            }
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return PAGES;
        }
    }
}
