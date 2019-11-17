package com.profesorfalken.jiittimer.gui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

public class AddWorkoutDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("Add Workout").setMessage("Give a name to your workout:");

        final EditText workoutNameEditText = new EditText(getActivity());
        workoutNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(workoutNameEditText);

        builder.setPositiveButton("Accept",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String workoutName = workoutNameEditText.getText().toString();
                        if (!workoutName.trim().isEmpty()) {
                            //((JIITActivity)getActivity()).addNewWorkout(workoutName);
                        }
                        dismiss();
                    }
                }
        )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismiss();
                            }
                        }
                );
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
