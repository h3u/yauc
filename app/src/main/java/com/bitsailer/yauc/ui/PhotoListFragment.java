package com.bitsailer.yauc.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bitsailer.yauc.Preferences;
import com.bitsailer.yauc.R;
import com.bitsailer.yauc.data.PhotoColumns;
import com.bitsailer.yauc.data.PhotoProvider;
import com.bitsailer.yauc.event.UserDataLoadedEvent;
import com.bitsailer.yauc.event.UserDataRemovedEvent;
import com.bitsailer.yauc.sync.PhotoManagement;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.R.attr.columnCount;
import static com.bitsailer.yauc.ui.PhotoType.FAVORITES;
import static com.bitsailer.yauc.ui.PhotoType.OWN;

/**
 * Fragment to display grid of photos.
 */
@SuppressWarnings("unused")
public class PhotoListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    protected static final String ARG_PHOTO_TYPE = "photo-type";
    private static final String KEY_MULTICOLUMNS = "key_multicolumns";
    private static final int LOADER_ID = 0;
    protected PhotoType mPhotoType;
    private static final String[] PHOTO_COLUMNS = {
            PhotoColumns.PHOTO_ID,
            PhotoColumns.PHOTO_COLOR,
            PhotoColumns.PHOTO_WIDTH,
            PhotoColumns.PHOTO_HEIGHT,
            PhotoColumns.URLS_SMALL,
            PhotoColumns.URLS_REGULAR,
            PhotoColumns.URLS_FULL
    };
    protected PhotoListAdapter mAdapter;
    protected Unbinder mButterKnife;
    private boolean mMultiColumn;

    @BindView(R.id.list)
    RecyclerView mRecyclerView;
    @BindView(R.id.emptyLayout)
    LinearLayout mEmptyLayout;
    @BindView(R.id.buttonSignIn)
    Button mButtonSignIn;
    @BindView(R.id.buttonFetch)
    Button mButtonFetch;
    @BindView(R.id.textViewEmpty)
    TextView mEmptyText;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhotoListFragment() {
    }

    public static PhotoListFragment newInstance(PhotoType type) {
        PhotoListFragment fragment = new PhotoListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PHOTO_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // enable menu for fragment
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mPhotoType = (PhotoType) getArguments().getSerializable(ARG_PHOTO_TYPE);
        }

        if (savedInstanceState != null) {
            mMultiColumn = savedInstanceState.getBoolean(KEY_MULTICOLUMNS, true);
        } else {
            // get setting from preferences
            mMultiColumn = Preferences.get(getActivity()).displayGrid(mPhotoType);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return setupFragment(
                inflater.inflate(R.layout.fragment_photo_list, container, false));
    }

    protected View setupFragment(View view) {

        mButterKnife = ButterKnife.bind(this, view);

        // Set the adapter
        mAdapter = new PhotoListAdapter(view.getContext(), columnCount,
                new PhotoListAdapter.PhotoOnClickHandler() {
                    @Override
                    public void onClick(String photoId, PhotoListAdapter.PhotoListItemViewHolder vh) {
                        ((ClickCallback) getActivity())
                                .onItemSelected(PhotoProvider.Uri.withId(photoId), vh);
                    }
                });
        mAdapter.setUseRegularPhotoSize(useRegularPhotoSize());
        setupLayout();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mButterKnife.unbind();
    }

    private void setupLayout() {
        // get column count
        int columnCount = getResources().getInteger(R.integer.photo_grid_columns);
        if (!mMultiColumn) {
            columnCount = 1;
        }

        // Set the adapter
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                columnCount, StaggeredGridLayoutManager.VERTICAL));
        mAdapter.setColumnCount(columnCount);
        mRecyclerView.setAdapter(mAdapter);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_MULTICOLUMNS, mMultiColumn);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_photo_list, menu);
        prepareMenu(menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // change menu item if grid is set or not
        prepareMenu(menu);
    }

    private void prepareMenu(Menu menu) {
        if (mMultiColumn) {
            menu.findItem(R.id.action_layout)
                    .setTitle(R.string.action_layout_list)
                    .setIcon(R.drawable.ic_action_list);
        } else {
            menu.findItem(R.id.action_layout)
                    .setTitle(R.string.action_layout_grid)
                    .setIcon(R.drawable.ic_action_grid);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_layout) {
            mMultiColumn = !mMultiColumn;
            Preferences.get(getActivity()).setDisplayGrid(mPhotoType, mMultiColumn);
            setupLayout();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * With a display width exceeding 1080px true is returned.
     *
     * @return answer to use regular photo size
     */
    private boolean useRegularPhotoSize() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);
        return displaymetrics.widthPixels > 1080;
    }

    /**
     * Interface that needs to be implemented by Activity which holds this Fragment to get notified
     * about item selection.
     */
    public interface ClickCallback {
        /**
         * Callback when an item has been selected.
         */
        @SuppressWarnings("UnusedParameters")
        void onItemSelected(Uri uri, PhotoListAdapter.PhotoListItemViewHolder vh);
    }

    public interface LoginCallback {
        /**
         * Callback when user pressed Sign In.
         */
        void onSignInSelected();
    }

    private String getSelection() {

        if (Preferences.get(getActivity()).getUserUsername() != null) {
            return PhotoColumns.PHOTO_LIKED_BY_USER + " = ? AND "
                    + PhotoColumns.USER_USERNAME + " <> ?";
        }
        return null;
    }

    private String[] getSelectionArgs() {
        if (Preferences.get(getActivity()).getUserUsername() != null) {
            return new String[]{
                    "0", // filter favorites
                    Preferences.get(getActivity()).getUserUsername() // filter own
            };
        }
        return null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mPhotoType == FAVORITES) {
            return new CursorLoader(getActivity(),
                    PhotoProvider.Uri.BASE,
                    PHOTO_COLUMNS,
                    PhotoColumns.PHOTO_LIKED_BY_USER + " = ?",
                    new String[] {"1"},
                    null);
        } else if (mPhotoType == OWN) {
            return new CursorLoader(getActivity(),
                    PhotoProvider.Uri.withUsername(Preferences.get(getActivity()).getUserUsername()),
                    PHOTO_COLUMNS,
                    null,
                    null,
                    null);
        } else {
            // type new photos
            return new CursorLoader(getActivity(),
                    com.bitsailer.yauc.data.PhotoProvider.Uri.BASE,
                    PHOTO_COLUMNS,
                    getSelection(),
                    getSelectionArgs(),
                    null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
        if (data.getCount() == 0) {
            emptyLayoutChange(true);
        } else {
            emptyLayoutChange(false);
        }
    }

    protected void emptyLayoutChange(boolean empty) {
        if (empty) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            if (Preferences.get(getContext()).isAuthenticated()) {
                mEmptyText.setText(getString(R.string.text_empty_photo_list));
                mButtonSignIn.setVisibility(View.GONE);
                mButtonFetch.setVisibility(View.VISIBLE);
            } else {
                mEmptyText.setText(getString(R.string.text_anonymous_photo_list));
                mButtonSignIn.setVisibility(View.VISIBLE);
                mButtonFetch.setVisibility(View.GONE);
            }
        } else {
            mEmptyLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @OnClick(R.id.buttonSignIn)
    public void onLoginClick() {
        ((LoginCallback) getActivity()).onSignInSelected();
    }

    @OnClick(R.id.buttonFetch)
    public void onFetchClick() {
        PhotoManagement
                .initUsersPhotos(getActivity(), Preferences.get(getActivity()).getUserUsername());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @SuppressWarnings("UnusedParameters")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserDataLoaded(UserDataLoadedEvent event) {
        restartLoader();
    }

    @SuppressWarnings("UnusedParameters")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserDataRemoved(UserDataRemovedEvent event) {
        restartLoader();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }
}
