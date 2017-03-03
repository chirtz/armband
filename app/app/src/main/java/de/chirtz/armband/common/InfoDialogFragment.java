package de.chirtz.armband.common;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import de.chirtz.armband.R;


public class InfoDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_REQUEST = "request";
    public static final String TAG = "InfoDialogFragment";
    private int requestId;

    public interface InfoDialogDismissListener {
        void onInfoDialogDismissed(int request);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        Bundle args = getArguments();
        String title = args.getString(ARG_TITLE, "");
        String message = args.getString(ARG_MESSAGE, "");
        requestId = args.getInt(ARG_REQUEST, -1);
        return new AlertDialog.Builder(getActivity(), R.style.Theme_Dialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof InfoDialogDismissListener) {
            ((InfoDialogDismissListener) activity).onInfoDialogDismissed(requestId);
        }
    }


    public static DialogFragment newInstance(String title, String message) {
        InfoDialogFragment f = new InfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        f.setArguments(args);
        return f;
    }

    public static DialogFragment newInstance(int request, String title, String message) {
        InfoDialogFragment f = new InfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putInt(ARG_REQUEST, request);
        f.setArguments(args);
        return f;
    }

}