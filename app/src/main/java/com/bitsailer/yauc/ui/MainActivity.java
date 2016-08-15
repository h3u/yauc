package com.bitsailer.yauc.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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
import com.bitsailer.yauc.api.UnsplashAPI;
import com.bitsailer.yauc.api.UnsplashService;
import com.bitsailer.yauc.api.model.User;
import com.bitsailer.yauc.sync.PhotoManagement;
import com.bitsailer.yauc.sync.SyncAdapter;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bitsailer.yauc.R.id.fab;
import static com.bitsailer.yauc.Util.AppStart.FIRST_TIME;

/**
 * App entry point and holder of three {@link PhotoListFragment} to
 * display new, favorite and own photos arranged in tabs.
 */
public class MainActivity extends AppCompatActivity implements PhotoListFragment.ClickCallback {

    private static final String STATE_TAB_POSITION = "state_tab_position";
    private static Preferences mPreferences;
    private int mTabPosition = SectionsPagerAdapter.POSITION_NEW;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @BindView(R.id.main_content) CoordinatorLayout mMainContent;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;
    @BindView(fab) FloatingActionButton mFab;

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
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        if (savedInstanceState != null) {
            mTabPosition = savedInstanceState
                    .getInt(STATE_TAB_POSITION, SectionsPagerAdapter.POSITION_NEW);
        }

        if (FIRST_TIME == Util.checkAppStart(this, mPreferences)) {
            welcome();
        }

        // todo: enable this after app approval (without hourly rate limit)
        // update users photos on app start
        /* if (savedInstanceState == null && mPreferences.isAuthenticated()) {
            PhotoManagement.updateUsersPhotos(MainActivity.this,
                    mPreferences.getUserUsername());
        } */

        // add listener and display fab
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add photo coming soon ...", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

        // setup icons
        initTabs(mTabPosition);
    }

    private void welcome() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_welcome_title)
                .setMessage(R.string.dialog_welcome_message);
        builder.setPositiveButton(R.string.button_dialog_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked Sign in button
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        builder.setNegativeButton(R.string.button_dialog_dismiss, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void arrange(TabLayout.Tab tab) {
        if (tab.getPosition() == SectionsPagerAdapter.POSITION_NEW) {
            if (tab.isSelected()) {
                tab.setIcon(R.drawable.ic_new_releases_accent);
                mFab.hide();
            } else {
                tab.setIcon(R.drawable.ic_new_releases);
            }
        } else if (tab.getPosition() == SectionsPagerAdapter.POSITION_FAVORITES) {
            if (tab.isSelected()) {
                tab.setIcon(R.drawable.ic_favorite_accent);
                mFab.hide();
            } else {
                tab.setIcon(R.drawable.ic_favorite);
            }
        } else if (tab.getPosition() == SectionsPagerAdapter.POSITION_OWN) {
            if (tab.isSelected()) {
                tab.setIcon(R.drawable.ic_account_accent);
                if (mPreferences.isAuthenticated()) {
                    mFab.show();
                }
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
                startActivity(new Intent(this, LoginActivity.class));
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
        mTabLayout.setOnTabSelectedListener(null);
        super.onStop();
    }

    /**
     * Greet user if there's an intent from login.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        // comeback from sign in
        if (intent != null && intent.hasExtra(LoginActivity.INTENT_EXTRA_SUCCESS)) {
            if (intent.getBooleanExtra(LoginActivity.INTENT_EXTRA_SUCCESS, true)) {
                sayHello();
            } else {
                loginError();
            }
            getIntent().removeExtra(LoginActivity.INTENT_EXTRA_SUCCESS);
        }

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTabPosition = tab.getPosition();
                initTabs(mTabPosition);
                arrange(tab);
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

    private void sayHello() {
        UnsplashAPI api = UnsplashService.create(UnsplashAPI.class, this);
        Call<User> call = api.getMe();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user = response.body();
                mPreferences.setUser(user);
                String name = user.getName();
                if (name == null || TextUtils.isEmpty(name)) {
                    name = getString(R.string.unknown_user_name);
                } else {
                    PhotoManagement
                            .updateUsersPhotos(MainActivity.this, user.getUsername());
                }
                Snackbar.make(mMainContent,
                        String.format(getString(R.string.message_login), name),
                        Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Logger.e(t.getMessage());
            }
        });

    }

    private void loginError() {
        Snackbar.make(mMainContent, R.string.message_failure_login, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(Uri uri, PhotoListAdapter.PhotoListItemViewHolder vh) {
        Intent intent = new Intent(this, DetailActivity.class)
                .setData(uri);
        startActivity(intent);
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
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
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

        public SectionsPagerAdapter(FragmentManager fm) {
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
