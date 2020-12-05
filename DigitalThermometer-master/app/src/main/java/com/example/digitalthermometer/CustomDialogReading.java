package com.example.digitalthermometer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CustomDialogReading extends Dialog implements android.view.View.OnClickListener {

    public Reading mReading;
    public Activity c;
    public Dialog d;

    private TextView temp, time, symptoms;
    private FormatHelpers formatHelpers = new FormatHelpers();

    public CustomDialogReading(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reading_details);

        time = (TextView) findViewById(R.id.view_reading_time);
        symptoms = (TextView) findViewById(R.id.view_reading_symptoms);

        ArrayList<Symptoms> symptomsArrayList = formatHelpers.fromSerializedSymptomsToSymptoms(mReading.symptoms);

        ArrayList<String> symptomStrings = formatHelpers.fromSymptomsToStrings(symptomsArrayList);
        StringBuilder mSymptoms = new StringBuilder();
        for (int i = 0; i < symptomStrings.size(); i++)
        {
            if (i == symptomStrings.size() - 1)
                mSymptoms.append(symptomStrings.get(i) + " ");
            else
                mSymptoms.append(symptomStrings.get(i) + ", ");
        }

        time.setText(mReading.time.toString());
        symptoms.setText(mSymptoms);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
        dismiss();
    }
}
