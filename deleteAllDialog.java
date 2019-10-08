package com.example.davidtruong.list;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

public class deleteAllDialog extends DialogFragment{
    //Make a dialog for deleting all the rows
    public interface deleteAllListener{
        public void onDelAllPositiveClick(DialogFragment dialog);
        public void onDelAllNegativeClick(DialogFragment dialog);
    }

    deleteAllListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete category?");
        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onDelAllPositiveClick(deleteAllDialog.this);
                    }
                }
        );
        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onDelAllNegativeClick(deleteAllDialog.this);
                    }
                }
        );
        return builder.create();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        mListener = (deleteAllListener) context;
    }
}
