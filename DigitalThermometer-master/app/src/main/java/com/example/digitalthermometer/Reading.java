package com.example.digitalthermometer;

import androidx.annotation.InspectableProperty;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;


@Entity(tableName = "readings")
public class Reading {

    public final String INTENT_IDENTIFIER_READING_ID = "READING_ID";

    @PrimaryKey
    @NonNull
    public Integer id;

    public Double temp;

    public Date time;

    public String symptoms;

    public Reading getReading(){
        Reading mReading = new Reading();
        mReading.id = this.id;
        mReading.temp = this.temp;
        mReading.time = this.time;
        mReading.symptoms = this.symptoms;

        return mReading;
    }

    public void setTemp(Double temp){
        this.temp = temp;
    }

    public void setTime(Date time){
        this.time = time;
    }

    public void setSymptoms(String symptoms) {this.symptoms = symptoms; }
}