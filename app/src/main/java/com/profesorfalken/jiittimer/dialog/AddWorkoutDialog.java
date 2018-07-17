package com.profesorfalken.jiittimer.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.profesorfalken.jiittimer.R;

public class AddWorkoutDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("Add Workout").setMessage("Give a name to your workout:");

        final EditText workoutName = new EditText(getActivity());
        workoutName.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(workoutName);
        builder.setPositiveButton("Accept",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getActivity(), "Acepted", Toast.LENGTH_LONG);
                    }
                }
        )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG);
                            }
                        }
                );
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
