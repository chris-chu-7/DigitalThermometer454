package com.example.digitalthermometer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.chip.ChipGroup;

import org.w3c.dom.Text;

import java.util.List;

public class PositiveReadingActivity extends AppCompatActivity {

    private Button btn_google_maps;
    private DbHelper mydb;

    private TextView title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_positive_reading);

        btn_google_maps = (Button) findViewById(R.id.redirect_google_maps_btn);


        btn_google_maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PositiveReadingActivity.this, MapsActivity.class));
            }
        });

        mydb = new DbHelper(this);

        Reading positiveReading = new Reading();
        String readingId = getIntent().getStringExtra(positiveReading.INTENT_IDENTIFIER_READING_ID);
        positiveReading = mydb.getReading(Integer.parseInt(readingId));

        title = (TextView) findViewById(R.id.positive_reading_title);
        title.setText(Double.toString(positiveReading.temp));
    }
}