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

/**
 * Fragment to display grid of photos.
 */
@SuppressWarnings("unused")
public class PhotoListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    protected static final String ARG_PHOTO_TYPE = "photo-type";
    private static final int LOADER_ID = 0;
    public static final int PHOTO_TYPE_NEW = 1;
    public static final int PHOTO_TYPE_FAVORITES = 2;
    public static final int PHOTO_TYPE_OWN = 3;
    protected int mPhotoType = PHOTO_TYPE_NEW;
    private static final String[] PHOTO_COLUMNS = {
            PhotoColumns.PHOTO_ID,
            PhotoColumns.PHOTO_COLOR,
            PhotoColumns.PHOTO_WIDTH,
            PhotoColumns.PHOTO_HEIGHT,
            PhotoColumns.URLS_SMALL
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

    public static PhotoListFragment newInstance(int photoType) {
        PhotoListFragment fragment = new PhotoListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PHOTO_TYPE, photoType);
        fragment.setArguments(args);
        fragment.setHasOptionsMenu(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPhotoType = getArguments().getInt(ARG_PHOTO_TYPE);
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

        // get user setting for columns
        mMultiColumn = Preferences.get(getActivity()).getLayoutPhotoGrid(mPhotoType);
        // get column count
        int columnCount = getResources().getInteger(R.integer.photo_grid_columns);
        if (!mMultiColumn) {
            columnCount = getResources().getInteger(R.integer.reduced_photo_columns);
        }

        // Set the adapter
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                columnCount, StaggeredGridLayoutManager.VERTICAL));

        mAdapter = new PhotoListAdapter(view.getContext(), columnCount,
                new PhotoListAdapter.PhotoOnClickHandler() {
                    @Override
                    public void onClick(String photoId, PhotoListAdapter.PhotoListItemViewHolder vh) {
                        ((ClickCallback) getActivity())
                                .onItemSelected(PhotoProvider.Uri.withId(photoId), vh);
                    }
                });
        mRecyclerView.setAdapter(mAdapter);
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

    private void changeColumnCount() {
        // get column count
        int columnCount = getResources().getInteger(R.integer.photo_grid_columns);
        if (!mMultiColumn) {
            columnCount = getResources().getInteger(R.integer.reduced_photo_columns);
        }

        // Set the adapter
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                columnCount, StaggeredGridLayoutManager.VERTICAL));
        mAdapter.setColumnCount(columnCount);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_photo_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // change menu item if grid is set or not
        if (Preferences.get(getActivity()).getLayoutPhotoGrid(mPhotoType)) {
            menu.findItem(R.id.action_layout)
                    .setTitle(R.string.action_layout_columns)
                    .setIcon(R.drawable.ic_action_grid);
        } else {
            menu.findItem(R.id.action_layout)
                    .setTitle(R.string.action_layout_column)
                    .setIcon(R.drawable.ic_action_list);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_layout) {
            if (mMultiColumn) {
                mMultiColumn = false;
            } else {
                mMultiColumn = true;
            }
            changeColumnCount();
        }
        return super.onOptionsItemSelected(item);
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
        if (mPhotoType == PHOTO_TYPE_FAVORITES) {
            return new CursorLoader(getActivity(),
                    PhotoProvider.Uri.BASE,
                    PHOTO_COLUMNS,
                    PhotoColumns.PHOTO_LIKED_BY_USER + " = ?",
                    new String[] {"1"},
                    null);
        } else if (mPhotoType == PHOTO_TYPE_OWN) {
            return new CursorLoader(getActivity(),
                    PhotoProvider.Uri.withUsername(Preferences.get(getActivity()).getUserUsername()),
                    PHOTO_COLUMNS,
                    null,
                    null,
                    null);
        } else {
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
