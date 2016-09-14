package com.bitsailer.yauc.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.bitsailer.yauc.YaucApplication;

/**
 * A simple {@link DialogFragment} subclass.
 * Use the {@link SimpleDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SimpleDialogFragment extends DialogFragment {

    protected static final String ARG_ID = "param_id";
    protected static final String ARG_TITLE = "param_title";
    protected static final String ARG_MESSAGE = "param_message";
    protected static final String ARG_NEGATIVE_BUTTON = "param_negative_button";
    protected static final String ARG_POSITIVE_BUTTON = "param_positive_button";

    protected String mParamTitle;
    protected String mParamMessage;
    protected String mParamNegativeButton;
    protected String mParamPositiveButton;
    protected int mId;

    public SimpleDialogFragment() {
        // Required empty public constructor
    }

    public interface PositiveClickListener {
        void onDialogPositiveClick(int id);
    }

    public interface NegativeClickListener {
        void onDialogNegativeClick(int id);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Dialog title.
     * @param message Dialog message.
     * @return A new instance of fragment SimpleDialogFragment.
     */
    public static SimpleDialogFragment newInstance(
            int id, String title, String message,
            String negativeButton, String positiveButton) {

        SimpleDialogFragment fragment = new SimpleDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_NEGATIVE_BUTTON, negativeButton);
        args.putString(ARG_POSITIVE_BUTTON, positiveButton);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mId = getArguments().getInt(ARG_ID);
            mParamTitle = getArguments().getString(ARG_TITLE);
            mParamMessage = getArguments().getString(ARG_MESSAGE);
            mParamNegativeButton = getArguments().getString(ARG_NEGATIVE_BUTTON);
            mParamPositiveButton = getArguments().getString(ARG_POSITIVE_BUTTON);
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(mParamTitle)
                .setMessage(mParamMessage)
                .setPositiveButton(mParamPositiveButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // positive button clicked
                        try {
                            ((PositiveClickListener) getActivity()).onDialogPositiveClick(mId);
                        } catch (Exception e) {
                            YaucApplication.reportException(e);
                            dismiss();
                        }
                    }
                })
                .setNegativeButton(mParamNegativeButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // negative button clicked
                        try {
                            ((NegativeClickListener) getActivity()).onDialogNegativeClick(mId);
                        } catch (Exception e) {
                            YaucApplication.reportException(e);
                            dismiss();
                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
