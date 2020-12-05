package com.example.digitalthermometer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.util.DBUtil;


import com.google.android.gms.common.util.Hex;
import com.google.android.gms.maps.GoogleMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.WordViewHolder> {
    private ArrayList<Reading> mWordList;

    private final String highTempDatabaseEntry = "#CD5C5C";
    private final String lightGrayDatabaseEntry = "#C0C0C0";
    private final String darkGrayDatabaseEntry = "#808080";
    private boolean alternateColor = true;

    private final Context context;
    private LayoutInflater mInflater;
    private FormatHelpers formatHelpers = new FormatHelpers();
    private DbHelper mydb;

    public WordListAdapter(Context context, ArrayList<Reading> wordList) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.mWordList = wordList;
        mydb = new DbHelper(context);
    }


    class WordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView wordItemView;
        public final TextView wordItemViewTitle;

        public final ImageButton moreInfo;
        public final ImageButton export;
        public final ImageButton edit;

        final WordListAdapter mAdapter;

        public WordViewHolder(View itemView, WordListAdapter adapter) {
            super(itemView);
            wordItemView = itemView.findViewById(R.id.bodyEntry);
            wordItemViewTitle = itemView.findViewById(R.id.titleEntry);
            moreInfo = itemView.findViewById(R.id.moreInfoEntry);
            export = itemView.findViewById(R.id.exportEntry);
            edit = itemView.findViewById(R.id.editEntry);

            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Ported over to more info button
        }
    }

    @NonNull
    @Override
    public WordListAdapter.WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.wordlist_item, parent, false);
        return new WordViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull WordListAdapter.WordViewHolder holder, final int position) {
        final Reading mCurrent = WordListAdapter.this.mWordList.get(position);

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

        ArrayList<String> output = formatHelpers.fromSerializedSymptomsToStrings(mCurrent.symptoms);

        String dynamicColor = dynamicColorFromTemperature(mCurrent.temp);
        int textColor = Color.parseColor(dynamicColor);

        // <font color=\"" + textColor + "\">" + mCurrent.temp.toString() + "°</font>"));
        holder.wordItemViewTitle.setText(Html.fromHtml("Reading taken on <b>" + df.format(mCurrent.time) + "</b>"));
        holder.wordItemView.setText(Html.fromHtml("Had temperature <b>" + mCurrent.temp.toString() + "°</b>"));

        if (alternateColor) {
            alternateColor = false;
            int color = Color.parseColor(lightGrayDatabaseEntry);
            holder.wordItemView.setBackgroundColor(color);
            holder.itemView.setBackgroundColor(color);
            holder.moreInfo.setBackgroundColor(color);
            holder.edit.setBackgroundColor(color);
            holder.export.setBackgroundColor(color);
        }
        else
        {
            alternateColor = true;
            int color = Color.parseColor(darkGrayDatabaseEntry);
            holder.wordItemView.setBackgroundColor(color);
            holder.itemView.setBackgroundColor(color);
            holder.moreInfo.setBackgroundColor(color);
            holder.edit.setBackgroundColor(color);
            holder.export.setBackgroundColor(color);
        }

        holder.moreInfo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Reading updatedReading = new Reading();
                updatedReading = mydb.getReading(mCurrent.id);

                CustomDialogReading dialogReading = new CustomDialogReading((Activity) v.getContext());
                dialogReading.mReading = new Reading();
                dialogReading.mReading.temp = updatedReading.temp;
                dialogReading.mReading.time = updatedReading.time;
                dialogReading.mReading.symptoms = updatedReading.symptoms;

                dialogReading.show();
            }
        });

        holder.export.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(mydb.exportDBtoCSV(mCurrent.id))
                    Toast.makeText(v.getContext(), "Successfully exported data", Toast.LENGTH_LONG).show();
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Reading updatedReading = new Reading();
                updatedReading = mydb.getReading(mCurrent.id);
                CustomDialogSymptoms dialogSymptoms = new CustomDialogSymptoms((Activity) v.getContext(), mCurrent.id, updatedReading.symptoms);
                dialogSymptoms.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWordList.size();
    }

    public Reading getReadingAtPosition(int position) {
        return mWordList.get(position);
    }

    void setReadings(ArrayList<Reading> readings) {
        mWordList = readings;
        notifyDataSetChanged();
    }

    // From Google.. normal body temperature is 98.6
    // As temperature deviates from that, change color accordingly.
    private String dynamicColorFromTemperature(double temp){
        double normalBodyTemp = 98.6;

        // 98% temperatures lie in this range
        double deviation = 4;

        double differenceBetweenReadingAndNormal = Math.abs(temp - normalBodyTemp);

        double percentage = differenceBetweenReadingAndNormal / deviation;

        double redPercentage = percentage;
        double greenPercentage = percentage;

        // Square percentage that is less to make colors more distinctly red/green
        if (percentage < .5) {
            redPercentage = Math.sqrt(redPercentage); // More green
            greenPercentage = greenPercentage * greenPercentage;
        }
        else {
            redPercentage = Math.sqrt(redPercentage);
            greenPercentage = greenPercentage * greenPercentage; // More red
        }

        int red = (int) Math.round(255 * redPercentage);
        int green = (int) Math.round(255 * (1 - greenPercentage));

        String hexRed;
        String hexGreen;
        // Have to do check for prepending 0s
        if (red <= 50)
            hexRed = "33";
        else
            hexRed = Integer.toHexString(red);

        if (green <= 50)
            hexGreen = "33";
        else
            hexGreen = Integer.toHexString(green);

        String hexColor = "#" + hexRed + hexGreen + "33";

        return hexColor;
    }



}
