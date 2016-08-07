package com.bitsailer.yauc.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bitsailer.yauc.Preferences;
import com.bitsailer.yauc.R;
import com.bitsailer.yauc.data.PhotoColumns;
import com.bitsailer.yauc.data.PhotoProvider;
import com.bitsailer.yauc.event.UserDataLoadedEvent;
import com.bitsailer.yauc.event.UserDataRemovedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Fragment to display list (column count = 1) or grid of photos.
 */
public class PhotoListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_PHOTO_TYPE = "photo-type";
    private static final int LOADER_ID = 0;
    static final int PHOTO_TYPE_NEW = 1;
    static final int PHOTO_TYPE_FAVORITES = 2;
    static final int PHOTO_TYPE_OWN = 3;
    private int mColumnCount = 2;
    private int mOrientation;
    private int mPhotoType = PHOTO_TYPE_NEW;
    private static final String[] PHOTO_COLUMNS = {
            PhotoColumns.PHOTO_ID,
            PhotoColumns.PHOTO_COLOR,
            PhotoColumns.PHOTO_WIDTH,
            PhotoColumns.PHOTO_HEIGHT,
            PhotoColumns.URLS_SMALL
    };
    private PhotoListAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhotoListFragment() {
    }

    @SuppressWarnings("unused")
    public static PhotoListFragment newInstance(int columnCount, int photoType) {
        PhotoListFragment fragment = new PhotoListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_PHOTO_TYPE, photoType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mPhotoType = getArguments().getInt(ARG_PHOTO_TYPE);
        }
        mOrientation = getResources().getConfiguration().orientation;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView
                        .setLayoutManager(new StaggeredGridLayoutManager(
                                mColumnCount, StaggeredGridLayoutManager.VERTICAL));
            }

            mAdapter = new PhotoListAdapter(context, null);

            recyclerView.setAdapter(mAdapter);
        }
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
                    PhotoProvider.Uri.FAVORITE,
                    PHOTO_COLUMNS,
                    null,
                    null,
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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
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
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserDataRemoved(UserDataRemovedEvent event) {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }
}
