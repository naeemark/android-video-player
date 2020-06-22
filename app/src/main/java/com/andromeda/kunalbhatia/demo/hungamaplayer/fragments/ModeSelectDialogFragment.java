package com.andromeda.kunalbhatia.demo.hungamaplayer.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.andromeda.kunalbhatia.demo.hungamaplayer.utils.ImageUtils;

import org.jetbrains.annotations.NotNull;

public class ModeSelectDialogFragment extends DialogFragment {

    private String[] picMode = {ImageUtils.PicModes.CAMERA, ImageUtils.PicModes.GALLERY};
    private String title = "Select Mode";
    private ModeSelectListener modeSelectListener;
    public static ModeSelectDialogFragment newInstance(String[] modes) {

        Bundle args = new Bundle();
        args.putStringArray("modes", modes);
        ModeSelectDialogFragment fragment = new ModeSelectDialogFragment();
        args.putString("title", fragment.title);
        fragment.setArguments(args);
        return fragment;
    }
    public static ModeSelectDialogFragment newInstance(String[] modes, String title) {

        Bundle args = new Bundle();
        args.putStringArray("modes", modes);
        args.putString("title", title);
        ModeSelectDialogFragment fragment = new ModeSelectDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null)
        {
            picMode = getArguments().getStringArray("modes");
            title = getArguments().getString("title");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setItems(picMode, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (modeSelectListener != null)
                            modeSelectListener.onModeSelected(picMode[which]);
                    }
                });
        return builder.create();
    }

    public void setModeSelectListener(ModeSelectListener modeSelectListener) {
        this.modeSelectListener = modeSelectListener;
    }

    public interface ModeSelectListener {
        void onModeSelected(String mode);
    }

}
