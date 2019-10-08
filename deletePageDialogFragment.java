package com.example.davidtruong.list;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

public class deletePageDialogFragment extends DialogFragment {
    public interface deletePageListener{
        public void onDelPagePosClick(DialogFragment dialog);
        public void onDelPageNegClick(DialogFragment dialog);
    }

    deletePageListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstance)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete page");
        builder.setMessage("Delete the selected page? This is permanent, and entries within " +
                "the page will be deleted permanently.");
        builder.setPositiveButton(
                "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onDelPagePosClick(deletePageDialogFragment.this);
                    }
                }
        );
        builder.setNegativeButton(
                "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onDelPageNegClick(deletePageDialogFragment.this);
                    }
                }
        );
        return builder.create();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        //Maybe do a try-catch
        mListener = (deletePageDialogFragment.deletePageListener) context;
    }
}
