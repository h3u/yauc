package com.bitsailer.yauc.ui;

import android.content.Context;
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
public class PhotoListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_PHOTO_TYPE = "photo-type";
    private static final int LOADER_ID = 0;
    static final int PHOTO_TYPE_NEW = 1;
    static final int PHOTO_TYPE_FAVORITES = 2;
    static final int PHOTO_TYPE_OWN = 3;
    private int mPhotoType = PHOTO_TYPE_NEW;
    private static final String[] PHOTO_COLUMNS = {
            PhotoColumns.PHOTO_ID,
            PhotoColumns.PHOTO_COLOR,
            PhotoColumns.PHOTO_WIDTH,
            PhotoColumns.PHOTO_HEIGHT,
            PhotoColumns.URLS_SMALL
    };
    private PhotoListAdapter mAdapter;
    private Unbinder butterknife;

    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.emptyLayout)
    LinearLayout emptyLayout;
    @BindView(R.id.buttonSignIn)
    Button buttonSignIn;
    @BindView(R.id.textViewEmpty)
    TextView emptyText;

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
        View view = inflater.inflate(R.layout.fragment_photo_list, container, false);
        butterknife = ButterKnife.bind(this, view);
        // get column count
        int columnCount = getResources().getInteger(R.integer.photo_grid_columns);
        if (mPhotoType == PHOTO_TYPE_FAVORITES) {
            columnCount = getResources().getInteger(R.integer.favorite_photo_columns);
        }

        // Set the adapter
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                columnCount, StaggeredGridLayoutManager.VERTICAL));

        mAdapter = new PhotoListAdapter(view.getContext(), null, columnCount,
                new PhotoListAdapter.PhotoOnClickHandler() {
                    @Override
                    public void onClick(String photoId, PhotoListAdapter.PhotoListItemViewHolder vh) {
                        ((ClickCallback) getActivity())
                                .onItemSelected(PhotoProvider.Uri.withId(photoId), vh);
                    }
                });
        recyclerView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        butterknife.unbind();
    }

    /**
     * Interface that needs to be implemented by Activity which holds this Fragment to get notified
     * about item selection.
     */
    public interface ClickCallback {
        /**
         * Callback when an item has been selected.
         */
        public void onItemSelected(Uri uri, PhotoListAdapter.PhotoListItemViewHolder vh);
    }

    public interface LoginCallback {
        /**
         * Callback when user pressed Sign In.
         */
        public void onSignInSelected();
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
        if (data.getCount() == 0 && mPhotoType != PHOTO_TYPE_NEW) {
            // data set empty
            emptyLayout.setVisibility(View.VISIBLE);
            if (Preferences.get(getContext()).isAuthenticated()) {
                emptyText.setText(getString(mPhotoType == PHOTO_TYPE_FAVORITES ?
                        R.string.text_empty_favorites : R.string.text_empty_own));
                buttonSignIn.setVisibility(View.GONE);
            } else {
                emptyText.setText(getString(R.string.text_anonymous_photo_list));
                buttonSignIn.setVisibility(View.VISIBLE);
            }
        } else {
            emptyLayout.setVisibility(View.GONE);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserDataLoaded(UserDataLoadedEvent event) {
        restartLoader();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserDataRemoved(UserDataRemovedEvent event) {
        restartLoader();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }
}
