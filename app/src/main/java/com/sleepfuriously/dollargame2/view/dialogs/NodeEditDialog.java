package com.sleepfuriously.dollargame2.view.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import java.util.Random;

import com.sleepfuriously.dollargame2.R;

/**
 * Displays a dialog that allows the user to modify the dollar amount
 * of a node and delete it.
 */
public class NodeEditDialog {

    //---------------------------
    //  constants
    //---------------------------

    //---------------------------
    //  data
    //---------------------------

    /** Holds the callback for when the dialog is complete */
    private OnNodeEditDialogDoneListener mListener;


    //---------------------------
    //  methods
    //---------------------------

    public void setOnNodeEditDialogDoneListener(OnNodeEditDialogDoneListener listener) {
        mListener = listener;
    }


    /**
     * Begins displaying the dialog.
     *
     * @param ctx   needed to get resources
     *
     * @param startDollars  The amount of dollars to display initially.
     */
    public void show(final Context ctx, final int startDollars) {

        // figure out this constant that will be used throughout
        final int seekbarOffset = ctx.getResources().getInteger(R.integer.DOLLAR_AMOUNT_SEEKBAR_OFFSET);

        LayoutInflater inflater = LayoutInflater.from(ctx);
        @SuppressLint("InflateParams")
        View inflatedView = inflater.inflate(R.layout.money_delete_dialog, null);

        final TextView dialogAmountTv = inflatedView.findViewById(R.id.dialog_amount);

        final SeekBar dialogSeekBar = inflatedView.findViewById(R.id.dialog_seekbar);
        dialogSeekBar.setProgress(startDollars + seekbarOffset);

        dialogSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String dollarStr = ctx.getString(R.string.dollar_number, progress - seekbarOffset);
                dialogAmountTv.setText(dollarStr);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        final Button dialogRandomButt = inflatedView.findViewById(R.id.dialog_rand_butt);
        dialogRandomButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                random.setSeed(System.currentTimeMillis());

                int randInt = random.nextInt((ctx.getResources().getInteger(R.integer.MAX_DOLLAR_AMOUNT_NON_NEGATIVE)) + 1);
                dialogSeekBar.setProgress(randInt);
            }
        });


        String initialDollarStr = ctx.getString(R.string.dollar_number,
                dialogSeekBar.getProgress() - seekbarOffset);
        dialogAmountTv.setText(initialDollarStr);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx);
        dialogBuilder.setView(inflatedView)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // call the listener signalling that the user cancelled.
                        mListener.result(true, startDollars, false);
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // call listener with proper data
                        mListener.result(false,
                                         dialogSeekBar.getProgress() - seekbarOffset,
                                         false);
                    }
                });

        final AlertDialog dialog = dialogBuilder.create();

        // Since this button needs to dismiss the dialog (which can only be
        // accessed through a Dialog, not a DialogBuilder), it is located here
        // near the end of the method when the dialog is actually instantiated.
        final Button deleteButt = inflatedView.findViewById(R.id.dialog_delete_butt);
        deleteButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // signal that this button is to be deleted
                mListener.result(false, startDollars, true);
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  interfaces
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public interface OnNodeEditDialogDoneListener {

        /**
         * Called when the dialog is closed by the user.
         *
         * @param cancelled     When TRUE, the user has cancelled any changes.
         *                      The other params should be ignored.
         *
         * @param dollarAmount  The dialog amount that the user has chosen.
         *                      Will be the same as the
         *
         * @param delete    When TRUE, means that the user wants to delete
         *                  this node.
         */
        void result(boolean cancelled, int dollarAmount, boolean delete);

    }

}
