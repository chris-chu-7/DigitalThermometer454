package com.example.digitalthermometer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReadingsActivity extends AppCompatActivity {

    private DbHelper mydb;
    private ListView obj;

    private RecyclerView mRecyclerView;
    private WordListAdapter mAdapter;
    TextView word;

    private boolean sorted = false;

    ImageButton btn_sort, btn_export;

    private ArrayList<Reading> mydbAllReadings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle("My Readings");

        word = (TextView) findViewById(R.id.bodyEntry);
        mRecyclerView = findViewById(R.id.recyclerview);

        mydb = new DbHelper(this);

        mydbAllReadings = mydb.getAllReadings();

        mAdapter = new WordListAdapter(this, mydbAllReadings);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper helper= new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder,
                                         int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Reading myItem = mAdapter.getReadingAtPosition(position);
                        Toast.makeText(ReadingsActivity.this, "Deleting Reading", Toast.LENGTH_LONG).show();
                        // Delete the reading
                        mydb.deleteReading(myItem.id);
                        mAdapter.setReadings(mydb.getAllReadings());
                        //mAdapter.notifyDataSetChanged();
                    }

                });
        helper.attachToRecyclerView(mRecyclerView);

        btn_sort = (ImageButton) findViewById(R.id.btn_sort);
        btn_export = (ImageButton) findViewById(R.id.btn_export);

        btn_sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext(), R.style.columnSortList);
                alertDialog.setTitle("Pick a column")
                        .setItems(R.array.columnList, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String[] mColumnArray = getResources().getStringArray(R.array.columnList);
                                String[] mColumnTypeArray = getResources().getStringArray(R.array.columnTypeList);
                                mAdapter.setReadings(mydb.sortReadings(mColumnArray[which].toLowerCase(), mColumnTypeArray[which]));
                            }
                        });
                alertDialog.setIcon(R.drawable.sort);
                alertDialog.show();

            }
        });

        btn_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mydb.exportDBtoCSV(-1))
                    Toast.makeText(ReadingsActivity.this, "Successfully exported data", Toast.LENGTH_LONG).show();
            }
        });
    }
}