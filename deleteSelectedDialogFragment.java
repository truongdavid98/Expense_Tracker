package com.example.davidtruong.list;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

public class deleteSelectedDialogFragment extends DialogFragment{
    //Make a dialogue and ask if the user wants to delete the selected rows
    public interface deleteSelListener{
        public void onDelSelPositiveClick(DialogFragment dialog);
        public void onDelSelNegativeClick(DialogFragment dialog);
    }

    deleteSelListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstance)
    {
        //Build the dialogFragment
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete selected rows?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            //When the yes button is pressed, call the corresponding function
            mListener.onDelSelPositiveClick(deleteSelectedDialogFragment.this);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            //When the no button is pressed, call the corresponding function
            mListener.onDelSelNegativeClick(deleteSelectedDialogFragment.this);
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        //Maybe do a try-catch
        mListener = (deleteSelListener) context;
    }

}
