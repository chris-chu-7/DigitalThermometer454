package com.example.digitalthermometer;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class NegativeReadingActivity extends AppCompatActivity {

    private Button btn_redirect_home;
    private ImageButton add_symptoms;
    private DbHelper mydb;
    private TextView title;
    private Reading negativeReading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_negative_reading);

        btn_redirect_home = (Button) findViewById(R.id.redirect_home_btn);
        add_symptoms = (ImageButton) findViewById(R.id.add_symptoms);


        mydb = new DbHelper(this);

        negativeReading = new Reading();
        final String readingId = getIntent().getStringExtra(negativeReading.INTENT_IDENTIFIER_READING_ID);
        negativeReading = mydb.getReading(Integer.parseInt(readingId));

        title = (TextView) findViewById(R.id.negative_reading_title);
        title.setText(Double.toString(negativeReading.temp));

        btn_redirect_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(NegativeReadingActivity.this, MainActivity.class));
            }
        });

        // Modal
        add_symptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fetch reading again as it might've been updated
                Reading updatedReading = new Reading();
                updatedReading = mydb.getReading(Integer.parseInt(readingId));
                CustomDialogSymptoms dialogSymptoms = new CustomDialogSymptoms(NegativeReadingActivity.this, Integer.parseInt(readingId), updatedReading.symptoms);
                dialogSymptoms.show();
            }
        });



    }
}