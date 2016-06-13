package com.parkapp.android.parkapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by levi on 13/6/16.
 */
public class SelectFavouriteDialogFragment extends DialogFragment {
    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    public interface SelectFavouriteDialogListener {
        public void onFavouriteDialogClick(DialogFragment dialog, int index);
    }


    CharSequence[] favouriteLabels;

    // Use this instance of the interface to deliver action events
    SelectFavouriteDialogListener mListener;

    public SelectFavouriteDialogFragment() {

    }

    public SelectFavouriteDialogFragment(List<Marker> favourites) {
        List<String> labels = new ArrayList<>();
        for (Marker marker : favourites) {
            labels.add(marker.getTitle());
        }
        this.favouriteLabels = labels.toArray(new CharSequence[labels.size()]);
    }

    // Override the Fragment.onAttach() method to instantiate the AddToFavouritesDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (SelectFavouriteDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pick_favourite)
                .setItems(this.favouriteLabels, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onFavouriteDialogClick(SelectFavouriteDialogFragment.this, which);
                    }
                });
        return builder.create();
    }
}
