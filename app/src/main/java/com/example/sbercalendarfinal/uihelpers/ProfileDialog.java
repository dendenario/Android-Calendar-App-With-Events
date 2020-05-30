package com.example.sbercalendarfinal.uihelpers;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sbercalendarfinal.CalendarViewWithNotesActivitySDK21;
import com.example.sbercalendarfinal.R;

import java.util.HashMap;
import java.util.Map;

public class ProfileDialog extends AppCompatDialogFragment {

    private String token;
    private Bundle args;
    private static final String URL_PROFILE = "http://95.131.148.98:8080/api/tasks";

    private EditText etEditName;
    private EditText etEditSurname;
    private EditText etEditWork;

    private String name;
    private String surname;
    private String work;

    private ProfileDialogListener profileDialogListener;



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //token = args.getString("token");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_editprofile, null);

        etEditName = view.findViewById(R.id.etEditName1);
        etEditSurname= view.findViewById(R.id.etEditSurname1);
        etEditSurname= view.findViewById(R.id.etEditWork1);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        name = etEditName.getText().toString();
                        surname = etEditSurname.getText().toString();
                        work = etEditWork.getText().toString();
                        profileDialogListener.applyTexts(name, surname, work);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        return builder.create();
    }

    public interface ProfileDialogListener
    {
        void applyTexts(String name, String surname, String work);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            profileDialogListener = (ProfileDialogListener) context;
        } catch (ClassCastException  e) {

        }
    }

}
