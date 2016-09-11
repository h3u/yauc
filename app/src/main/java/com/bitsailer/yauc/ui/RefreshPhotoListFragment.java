package com.bitsailer.yauc.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bitsailer.yauc.R;
import com.bitsailer.yauc.sync.SyncAdapter;

import butterknife.BindView;

import static com.bitsailer.yauc.R.id.swipeRefreshLayout;

/**
 * Display a list or grid of photos that can be refreshed with
 * a SwipeRefreshLayout.
 */
public class RefreshPhotoListFragment extends PhotoListFragment {

    @BindView(swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    public RefreshPhotoListFragment() {
    }

    public static PhotoListFragment newInstance(int photoType) {
        RefreshPhotoListFragment fragment = new RefreshPhotoListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PHOTO_TYPE, photoType);
        fragment.setHasOptionsMenu(true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = setupFragment(
                inflater.inflate(R.layout.fragment_refresh_photo_list, container, false));

        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    SyncAdapter.syncNow(getActivity());
                }
            });
        }

        return view;
    }

    private final BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SyncAdapter.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                notifySwipeRefreshingStatus(
                        intent.getBooleanExtra(SyncAdapter.EXTRA_REFRESHING, false));
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(mRefreshingReceiver,
                new IntentFilter(SyncAdapter.BROADCAST_ACTION_STATE_CHANGE));

    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mRefreshingReceiver);
    }

    private void notifySwipeRefreshingStatus(boolean refreshing) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    protected void emptyLayoutChange(boolean empty) {
        if (empty) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mEmptyText.setText(getString(R.string.text_empty_photo_list_swipe_to_refresh));
        } else {
            mEmptyLayout.setVisibility(View.GONE);
        }
    }
}
