package com.example.digitalthermometer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.solver.widgets.Helper;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class CustomDialogSymptoms extends Dialog implements android.view.View.OnClickListener {
    public Activity c;
    public Dialog d;
    public Button btn_submit;
    private ChipGroup symptom_list;
    private Integer readingId;
    private ArrayList<Symptoms> selectedSymptoms;
    private DbHelper mydb;
    private FormatHelpers formatHelpers = new FormatHelpers();

    public CustomDialogSymptoms(Activity a, Integer readingId, String selectedSymptoms) {
        super(a);
        this.c = a;
        this.readingId = readingId;
        this.selectedSymptoms = formatHelpers.fromSerializedSymptomsToSymptoms(selectedSymptoms);
        mydb = new DbHelper(c);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.symptom_list);
        btn_submit = (Button) findViewById(R.id.submit_btn);
        btn_submit.setOnClickListener(this);

        symptom_list = (ChipGroup) findViewById(R.id.symptom_list);

        // Check for previously selected symptoms
        // Have them be already checked
        for (int i = 0; i < selectedSymptoms.size(); i++) {
            Symptoms enumSymptom = selectedSymptoms.get(i);
            if (enumSymptom == Symptoms.None)
                continue;

            Chip symptom = (Chip) symptom_list.getChildAt(enumSymptom.ordinal());
            symptom.setChecked(true);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit_btn:
                symptom_list = (ChipGroup) findViewById(R.id.symptom_list);
                final List<Integer> selected_symptoms = symptom_list.getCheckedChipIds();

                // Converts a Chips tag to Symptom's enum number
                ArrayList<Symptoms> inputArray = new ArrayList<Symptoms>();
                for (int id : selected_symptoms) {
                    Chip symptom = (Chip) findViewById(id);
                    inputArray.add( Symptoms.fromInteger( Integer.parseInt((String) symptom.getTag())));
                }

                // Converts Symptoms Enum to JSON String for SQLite storing
                Gson gson = new Gson();

                String inputString = gson.toJson(inputArray);

                Reading mReading = mydb.getReading(readingId);
                mReading.setSymptoms(inputString);

                mydb.updateReading(mReading, String.valueOf(readingId));
                break;
            default:
                break;
        }
        dismiss();
    }
}
