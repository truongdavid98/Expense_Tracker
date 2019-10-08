package com.example.davidtruong.list;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class editRows extends AppCompatActivity {

    public static final String EDITTAG = "Edit_entry";
    public static String category;
    String rowid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_rows);
        Toolbar myToolbar = findViewById(R.id.editRowsToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Edit row");
        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.edit)));
        //Get the row that is selected
        Intent callingIntent = this.getIntent();
        category = callingIntent.getStringExtra("category");
        ab.setSubtitle(category);
        rowid = callingIntent.getStringExtra("selectedRow");
        Cursor selectedCursor = MainActivity.maindb.query(
                category + "table",
                null,
                "rowid=?",
                new String [] {rowid},
                null,
                null,
                null
        );
        selectedCursor.moveToNext();
        String detail = selectedCursor.getString(selectedCursor.getColumnIndex(databaseHelper.tableCol1));
        Integer amt = selectedCursor.getInt(selectedCursor.getColumnIndex(databaseHelper.tableCol2));
        //Put up the info into the old entry
        TextView oldDetail = new TextView(this);
        TextView oldAmt = new TextView(this);
        Button cancelBut = new Button(this);
        MainActivity.setTextView(oldDetail, detail, Color.BLACK);
        MainActivity.setTextView(oldAmt, amt.toString(), Color.BLACK);
        MainActivity.setButton(cancelBut, "cancel", Color.BLACK);
        moreDetails.setDetailView(oldDetail);
        oldDetail.setMaxWidth(400);
        oldDetail.setPadding(0,0,0,25);
        cancelBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Don't change the entry
                Intent cancelIntent = new Intent(editRows.this, moreDetails.class);
                cancelIntent.putExtra("wasEdit", "cancelled");
                cancelIntent.putExtra("category", category);
                startActivity(cancelIntent);
            }
        });
        //Create a row and insert it into the table
        TableLayout oldTable = findViewById(R.id.oldDetailsTable);
        TableRow oldRow = new TableRow(this);
        oldRow.addView(oldDetail);
        oldRow.addView(oldAmt);
        oldRow.addView(cancelBut);
        oldTable.addView(oldRow);
        //Now set up the hints
        EditText newDetail = findViewById(R.id.editDetail);
        EditText newAmt = findViewById(R.id.editAmt);
        newDetail.setGravity(Gravity.CENTER_HORIZONTAL);
        newAmt.setGravity(Gravity.CENTER_HORIZONTAL);
        newDetail.setHint(detail);
        newAmt.setHint(amt.toString());
        moreDetails.setDetailView(newDetail);
        newDetail.setMaxWidth(305);
        //Free up resources
        selectedCursor.close();
    }

    public void updateEntry(View v)
    {
        //Get the information put into the text boxes
        EditText detailView = findViewById(R.id.editDetail);
        EditText amtView = findViewById(R.id.editAmt);
        String detail = detailView.getText().toString();
        String amtText = amtView.getText().toString();
        if (amtText.isEmpty())
        {
            //Tell the user of the issue
            hideKeyboard();
            Snackbar emptySnack = Snackbar.make(
                    findViewById(R.id.editTitle),
                    "Please enter an amount",
                    Snackbar.LENGTH_SHORT
            );
            emptySnack.show();
            return;
        }
        String formatAmt = MainActivity.amtParse(amtText);
        if (formatAmt == null)
        {
            //Amt isn't in a proper format
            hideKeyboard();
            Snackbar amtSnack = Snackbar.make(
                    findViewById(R.id.editTitle),
                    "Please enter a proper amount",
                    Snackbar.LENGTH_SHORT
            );
            amtSnack.show();
            return;
        }
        Float amt = Float.parseFloat(formatAmt);
        if (v == findViewById(R.id.editMinus))
        {
            //If the amt passed in was negative, make it so
            amt *= -1;
        }
        //Make a contentValue to use for updating the entry
        ContentValues content = new ContentValues();
        content.put(databaseHelper.tableCol1, detail);
        content.put(databaseHelper.tableCol2, amt);
        //Now update the entry
        Integer numUpdated = MainActivity.maindb.update(
                category + "table",
                content,
                "rowid=?",
                new String[] {rowid}
        );
        Log.d(EDITTAG, "Num of rows updated: " + numUpdated);
        //Now go back to moreDetails activity
        Intent newIntent = new Intent(this, moreDetails.class);
        newIntent.putExtra("wasEdit", "edited");
        newIntent.putExtra("category", category);
        startActivity(newIntent);
    }


    public void hideKeyboard()
    {
        //Hide the keyboard
        TextView view = findViewById(R.id.editTitle);
        InputMethodManager keyHider = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyHider.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
