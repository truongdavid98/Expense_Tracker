package com.example.davidtruong.list;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class addEntry extends AppCompatActivity {
    private static final String INSERTTAG = "Insertion_addEntry: ";
    String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);
        Toolbar myToolbar = findViewById(R.id.addEntryToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Add entry");
        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.add)));
        //Set the category name TextView
        Intent callingIntent = getIntent();
        TextView catTextView = findViewById(R.id.entryCat);
        categoryName = callingIntent.getStringExtra("category");
        ab.setSubtitle(categoryName);
        catTextView.setText(categoryName);
    }

    public void onButtonPress(View view)
    {
        //Get the information from the entry screen
        Log.d(INSERTTAG, "Get the info from the screen");
        EditText detailView = findViewById(R.id.entryDetail);
        String detail = detailView.getText().toString();
        EditText inputAmt = findViewById(R.id.entryAmt);
        String amtText = inputAmt.getText().toString();
        if (amtText.isEmpty())
        {
            Log.d(INSERTTAG, "Go to error screen");
            hideKeyboard();
            updateErrorScreen();
            return;
        }
        String formattedAmtText = MainActivity.amtParse(amtText);
        if (formattedAmtText == null)
        {
            Snackbar amtSnack = Snackbar.make(
                    findViewById(R.id.addEntryTitle),
                    "Please enter a proper amount",
                    Snackbar.LENGTH_SHORT
            );
            amtSnack.show();
            return;
        }
        Float amt = Float.parseFloat(formattedAmtText);
        if (view == findViewById(R.id.entryMinus))
        {
            //If - was pressed, make the value negative
            amt *= -1;
        }
        Log.d(INSERTTAG, "Amt is: " + amt);

        //Add it to the table
        Log.d(INSERTTAG, "Make the content and add it in");
        ContentValues content = new ContentValues();
        content.put(databaseHelper.tableCol1, detail);
        content.put(databaseHelper.tableCol2, amt);

        //Now do the insertion
        Log.d(INSERTTAG, "Add the entry to the table");
        MainActivity.maindb.insert(categoryName + "table", null, content);
        Log.d(INSERTTAG, "Insertion successful");

        //Now go back to Main
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

    public void updateErrorScreen()
    {
        //Use this to display a Snackbar error message
        Snackbar errorSnack = Snackbar.make(
                findViewById(R.id.editTitle),
                "Please insert an amount",
                Snackbar.LENGTH_SHORT);
        errorSnack.show();
    }

    public void hideKeyboard()
    {
        //Hide the keyboard
        TextView view = findViewById(R.id.addEntryTitle);
        InputMethodManager keyHider = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyHider.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
