package com.example.davidtruong.list;

import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class graphPercentages extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_percentages);
        Toolbar toolbar = findViewById(R.id.graphToolbar);
        setSupportActionBar(toolbar);
        //Testing
        pieChartDraw test = new pieChartDraw(this);
        //setContentView(test);
    }

}
