package com.bitsailer.yauc.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
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
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bitsailer.yauc.Preferences;
import com.bitsailer.yauc.R;
import com.bitsailer.yauc.Util;
import com.bitsailer.yauc.YaucApplication;
import com.bitsailer.yauc.event.NetworkErrorEvent;
import com.bitsailer.yauc.event.UserLoadedEvent;
import com.bitsailer.yauc.sync.PhotoManagement;
import com.bitsailer.yauc.sync.SyncAdapter;
import com.bitsailer.yauc.widget.NewPhotosWidgetIntentService;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Intent.ACTION_SENDTO;
import static com.bitsailer.yauc.Util.AppStart.FIRST_TIME;
import static com.bitsailer.yauc.ui.PhotoType.*;
import static com.bitsailer.yauc.widget.NewPhotosWidget.EXTRA_NUM_PHOTOS;

/**
 * App entry point and holder of three {@link PhotoListFragment} to
 * display new, favorite and own photos arranged in tabs.
 */
@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity implements
        PhotoListFragment.ClickCallback, PhotoListFragment.LoginCallback,
        SimpleDialogFragment.PositiveClickListener,
        SimpleDialogFragment.NegativeClickListener {

    private static final String STATE_TAB_POSITION = "state_tab_position";
    private static final int RC_LOGIN_ACTIVITY = 4711;
    private static final int RC_INVITE = 4712;
    private static final int DIALOG_LOGIN = 0;
    private static final String TAG_DIALOG_LOGIN = "tag_dialog_login";

    private static Preferences mPreferences;
    private int mTabPosition = NEW.getTabPosition();

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
                    .getInt(STATE_TAB_POSITION, NEW.getTabPosition());
        }

        if (FIRST_TIME == Util.checkAppStart(this, mPreferences)
                && !mPreferences.isAuthenticated()) {
            welcome();
            trackScreenSize();
        }

        // update users photos on to keep things synchronized
        if (savedInstanceState == null && mPreferences.isAuthenticated()) {
            PhotoManagement.syncUsersPhotos(MainActivity.this,
                    mPreferences.getUserUsername());
        }

        // reset widget
        Intent intentService = new Intent(this, NewPhotosWidgetIntentService.class);
        intentService.putExtra(EXTRA_NUM_PHOTOS, 0);
        this.startService(intentService);

        // setup icons
        initTabs(mTabPosition);

        trackSelectedTab();
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
        if (tab.getPosition() == NEW.getTabPosition()) {
            if (tab.isSelected()) {
                tab.setIcon(R.drawable.ic_new_releases_accent);
            } else {
                tab.setIcon(R.drawable.ic_new_releases);
            }
        } else if (tab.getPosition() == FAVORITES.getTabPosition()) {
            if (tab.isSelected()) {
                tab.setIcon(R.drawable.ic_favorite_accent);
            } else {
                tab.setIcon(R.drawable.ic_favorite);
            }
        } else if (tab.getPosition() == OWN.getTabPosition()) {
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
                if (i == NEW.getTabPosition()) {
                    tab.setContentDescription(R.string.content_description_tab_new);
                } else if (i == FAVORITES.getTabPosition()) {
                    tab.setContentDescription(R.string.content_description_tab_favorite);
                } else if (i == OWN.getTabPosition()) {
                    tab.setContentDescription(R.string.content_description_tab_own);
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
                try {
                    startLogin();
                } catch (Exception e) {
                }
            }
            return true;
        }
        if (id == R.id.action_invite) {
            Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                    .setMessage(getString(R.string.invitation_message))
                    .setEmailHtmlContent(getString(R.string.invitation_email_body))
                    .setEmailSubject(getString(R.string.invitation_email_subject))
                    .setAndroidMinimumVersionCode(Build.VERSION_CODES.KITKAT)
                    .build();
            startActivityForResult(intent, RC_INVITE);
        }
        if (id == R.id.action_feedback) {
            Intent feedback = new Intent(ACTION_SENDTO);
            feedback.setData(Uri.parse("mailto:"));
            feedback.putExtra(Intent.EXTRA_EMAIL, new String[] {
                    ((YaucApplication)getApplication()).getFirebaseRemoteConfig()
                            .getString("feedback_email")
            });
            feedback.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_subject));
            if (feedback.resolveActivity(getPackageManager()) != null) {
                startActivity(feedback);
            }
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
        if (requestCode == RC_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Logger.d("onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                Logger.e("invite failed: %d", resultCode);
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
                trackSelectedTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == NEW.getTabPosition()) {
                    tab.setIcon(R.drawable.ic_new_releases);
                } else if (tab.getPosition() == FAVORITES.getTabPosition()) {
                    tab.setIcon(R.drawable.ic_favorite);
                } else if (tab.getPosition() == OWN.getTabPosition()) {
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
     * Analytics track tab visibility
     */
    private void trackSelectedTab() {
        String name = String.format("Tab%s", getTabName(mTabPosition));
        FirebaseAnalytics tracker = ((YaucApplication) getApplication()).getDefaultTracker();
        int orientation = getResources().getConfiguration().orientation;
        Bundle bundle = new Bundle();
        bundle.putString(YaucApplication.FB_PARAM_TAB_NAME, name);
        bundle.putString(YaucApplication.FB_PARAM_ORIENTATION,
                orientation == Configuration.ORIENTATION_LANDSCAPE ?
                        YaucApplication.FB_PARAM_ORIENTATION_LANDSCAPE :
                        YaucApplication.FB_PARAM_ORIENTATION_PORTRAIT);
        tracker.logEvent(YaucApplication.FB_EVENT_PHOTO_LIST_TAB_VISITED, bundle);
    }

    /**
     * Analytics track screen size
     */
    private void trackScreenSize() {
        FirebaseAnalytics tracker = ((YaucApplication) getApplication()).getDefaultTracker();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        Bundle bundle = new Bundle();
        bundle.putString(YaucApplication.FB_PARAM_DEVICE_SCREEN_WIDTH, Integer.toString(width));
        bundle.putString(YaucApplication.FB_PARAM_DEVICE_SCREEN_HEIGHT, Integer.toString(height));
        tracker.logEvent(YaucApplication.FB_EVENT_APP_FIRST_OPEN, bundle);
    }

    private String getTabName(int tabPosition) {
        if (tabPosition == NEW.getTabPosition()) {
            return getString(R.string.ga_name_tab_new);
        } if (tabPosition == FAVORITES.getTabPosition()) {
            return getString(R.string.ga_name_tab_favorite);
        } else if (tabPosition == OWN.getTabPosition()) {
            return getString(R.string.ga_name_tab_own);
        } else {
            return "";
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

    public void onDialogNegativeClick(int id) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        static final int PAGES = 3;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if (position == PhotoType.NEW.getTabPosition()) {
                return RefreshPhotoListFragment.newInstance(NEW);
            } else if (position == FAVORITES.getTabPosition()) {
                return PhotoListFragment.newInstance(FAVORITES);
            } else if (position == OWN.getTabPosition()) {
                return PhotoListFragment.newInstance(OWN);
            }
            return new Fragment();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return PAGES;
        }
    }
}
