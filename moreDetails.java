package com.example.davidtruong.list;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class moreDetails extends AppCompatActivity implements deleteSelectedDialogFragment.deleteSelListener,
        deleteAllDialog.deleteAllListener
{
    String categoryName;
    final String DETAILTAG = "More_details";
    ArrayList<String> selectedRows = new ArrayList<String>();
    Boolean needsVacuum = false;
    static Float selectedTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_details);
        Toolbar myToolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("More details");
        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.detail)));
        //From the calling intent, get the category name
        Intent callingIntent = getIntent();
        categoryName = callingIntent.getStringExtra("category");
        if (categoryName == null)
        {
            //That means the intent was editRows
            categoryName = editRows.category;
        }
        //Replace the underscores back to spaces
        String categoryDisplay = categoryName.trim().replace('_' , ' ').toLowerCase();
        ab.setSubtitle(categoryDisplay);
        String wasEdited = callingIntent.getStringExtra("wasEdited");
        if (wasEdited != null) {
            //If the string isn't null, notify the user accordingly
            if (wasEdited.equals("edited")) {
                Snackbar wasEditedSnack = Snackbar.make(
                        findViewById(R.id.moreDetailsTitle),
                        "Entry was edited successfully.",
                        Snackbar.LENGTH_SHORT
                );
                wasEditedSnack.show();
            }
            else
            {
                Snackbar wasCancelledSnack = Snackbar.make(
                        findViewById(R.id.moreDetailsTitle),
                        "Editing was cancelled.",
                        Snackbar.LENGTH_SHORT
                );
                wasCancelledSnack.show();
            }
        }
        TextView title = findViewById(R.id.moreDetailsTitle);
        title.setText(categoryDisplay);
        //Get all the data from the table
        Log.d(DETAILTAG, "Getting all the data from the underlying table.");
        Cursor allDataCursor = MainActivity.maindb.query(
                categoryName + "table",
                new String[] {"rowid", "Detail", "Amount", "Date"},
                null,
                null,
                null,
                null,
                null,
                null
        );
        String temps = DatabaseUtils.dumpCursorToString(allDataCursor);
        Log.d(DETAILTAG, temps);
        //Make the table and populate it based on the info from the cursor
        TableLayout table = findViewById(R.id.moreDetailsTable);
        Log.d(DETAILTAG, "Table made. Now make the rows.");
        while(allDataCursor.moveToNext())
        {
            //Get the info from the cursor
            String detail = allDataCursor.getString(allDataCursor.getColumnIndex(databaseHelper.tableCol1));
            Float amt = allDataCursor.getFloat(allDataCursor.getColumnIndex(databaseHelper.tableCol2));
            String date = allDataCursor.getString(allDataCursor.getColumnIndex(databaseHelper.tableCol3));
            Integer rowId = allDataCursor.getInt(allDataCursor.getColumnIndex("rowid"));
            //If needed, print the cursor's values out
            //String temp = DatabaseUtils.dumpCursorToString(allDataCursor);
            //Log.d(DETAILTAG, temp);
            //Initialize the components needed for the tableRow
            TableRow newRow = new TableRow(this);
            TextView detailView = new TextView(this);
            TextView amtView = new TextView(this);
            TextView dateView = new TextView(this);
            CheckBox selectBox = new CheckBox(this);
            //Associate the rowId and amt with the checkbox, used for another function
            selectBox.setTag(R.id.tagRowid, rowId);
            selectBox.setTag(R.id.tagAmt, amt);
            selectBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //Code for select button
                    CheckBox select = (CheckBox) v;
                    Integer rowId = (Integer) select.getTag(R.id.tagRowid);
                    Float tagAmt = (Float) select.getTag(R.id.tagAmt);
                    String totalText;
                    TextView selTotView = findViewById(R.id.selectedTotal);
                    if (select.isChecked())
                    {
                        //Put into the selectedRows array the rowId
                        Log.d(DETAILTAG, "Checked.");
                        selectedRows.add(rowId.toString());
                        //Update the selectedTotal
                        selectedTotal += tagAmt;
                    }
                    else
                    {
                        //Remove from selectedRows the rowId associated with this view
                        Log.d(DETAILTAG, "Unchecked");
                        Boolean temp = selectedRows.remove(rowId.toString());
                        if (temp)
                        {
                            Log.d(DETAILTAG, "Item successfully removed.");
                        }
                        else
                        {
                            Log.d(DETAILTAG, "Item not removed?");
                        }
                        //Update the selectedTotal
                        selectedTotal -= tagAmt;
                    }
                    //Update the selectedTotal textView
                    totalText = String.format(Locale.US, "%.2f", selectedTotal);
                    selTotView.setText(totalText);
                }
            });

            //Add the info into the textViews
            Log.d(DETAILTAG, "Setting the properties of the textViews.");
            MainActivity.setTextView(detailView, detail, Color.BLACK);
            MainActivity.setTextView(amtView, String.format(Locale.US, "%.2f", amt), Color.BLACK);
            MainActivity.setTextView(dateView, date, Color.BLACK);
            setDetailView(detailView);
            selectBox.setText("");

            //Add the stuff to the table row and add that to the table
            Log.d(DETAILTAG, "Add the stuff for the tableRow and table.");
            newRow.addView(detailView);
            newRow.addView(amtView);
            newRow.addView(dateView);
            newRow.addView(selectBox);

            table.addView(newRow);
        }
        Log.d(DETAILTAG, "All rows made successfully.");
        //Set up the selected total
        selectedTotal = Float.valueOf(0);
        TextView selTotView = findViewById(R.id.selectedTotal);
        selTotView.setText("0");
        //Free up resources
        allDataCursor.close();
    }

    @Override
    protected void onDestroy()
    {
        //On destroy, free up the cursor
        if (!isFinishing())
        {
            //The activity isn't finished, so vacuum later
            return;
        }
        Log.d(DETAILTAG, "On destroy.");
        if (needsVacuum)
        {
            //Vacuum if needed
            MainActivity.maindb.execSQL(
                    "VACUUM;"
            );
            Log.d(DETAILTAG, "Vacuum completed.");
        }
        super.onDestroy();
    }

    public static void setDetailView(TextView detailView)
    {
        detailView.setMaxWidth(500);
        detailView.setMaxLines(50);
        detailView.setMaxHeight(1000);
        detailView.setPadding(0,0,0,15);
        detailView.setGravity(Gravity.LEFT);
    }

    public void deleteSelected(View v) {
        //Remove all the rows that are selected
        if (selectedRows.isEmpty()) {
            Log.d(DETAILTAG, "Empty");
            Snackbar emptySnack = Snackbar.make(findViewById(R.id.moreDetailsTitle),
                    "No rows are selected", Snackbar.LENGTH_SHORT);
            emptySnack.show();
            return;
        }

        //Make a dialogue and ask if the user wants to delete the selected rows
        DialogFragment dialogue = new deleteSelectedDialogFragment();
        dialogue.show(getFragmentManager(), "Delete selected");
    }

    @Override
    public void onDelSelPositiveClick(DialogFragment dialog)
    {
        //Delete the selected Rows
        String [] selectedArray = selectedRows.toArray(new String[selectedRows.size()]);
        //Grab the current total
        Cursor totalCursor = MainActivity.maindb.query(
                databaseHelper.tableName,
                new String [] {databaseHelper.dbcol2},
                databaseHelper.dbcol1 + "=?",
                new String [] {categoryName},
                null,
                null,
                null
        );
        totalCursor.moveToFirst();
        Float currTotal = totalCursor.getFloat(totalCursor.getColumnIndex(databaseHelper.dbcol2));
        Log.d(DETAILTAG, "Starting total: " + currTotal);
        totalCursor.close();
        //Make a string for formatting the sqlite statement...
        String whereClause = "(";
        for (Integer i = 0; i < (selectedRows.size() - 1); i++)
        {
            whereClause += "?,";
        }
        whereClause += "?)";

        //Get the selected rows
        Cursor selectedCursor = MainActivity.maindb.query(
                categoryName + "table",
                new String [] {databaseHelper.tableCol2},
                "rowid in " + whereClause,
                selectedArray,
                null,
                null,
                null
        );
        while (selectedCursor.moveToNext())
        {
            currTotal -= selectedCursor.getFloat(selectedCursor.getColumnIndex(databaseHelper.tableCol2));
        }
        selectedCursor.close();
        Log.d(DETAILTAG, "Ending total: " + currTotal);
        ContentValues newTotal = new ContentValues();
        newTotal.put(databaseHelper.dbcol2, currTotal);
        Integer numUpdated = MainActivity.maindb.update(
                databaseHelper.tableName,
                newTotal,
                databaseHelper.dbcol1 + "=?",
                new String [] {categoryName}
        );
        Log.d(DETAILTAG, "Rows updated: " + numUpdated);
        //Delete the selected from the underlying table
        Log.d(DETAILTAG, "Where clause: " + whereClause);
        Integer holder = MainActivity.maindb.delete(categoryName + "table", "rowid IN " + whereClause, selectedArray);
        Log.d(DETAILTAG, "Rows deleted: " + holder);
        //Make an integer array out of selected array for ease of deletion
        Integer[] selectedIntegers = new Integer[selectedArray.length];
        for (Integer p = 0; p < selectedArray.length; p++)
        {
            selectedIntegers[p] = Integer.parseInt(selectedArray[p]);
        }
        //Sort the array to set up for faster deleting of rows
        Arrays.sort(selectedIntegers);
        //Now delete the rows from the table
        TableLayout detailTable = findViewById(R.id.moreDetailsTable);
        Integer m = 0;
        TableRow childRow;
        TextView childSelect;
        Integer rowid;
        //Start at k = 1, because the titles at the top are a part of the child at index 0
        for (Integer k = 1; k < detailTable.getChildCount() && m < selectedIntegers.length; k++)
        {
            //Go through each child row and see if it is the proper row to delete
            //Since both the table and the table is sorted by rowid, we don't have to reloop for later indices
            childRow = (TableRow) detailTable.getChildAt(k);
            childSelect = (CheckBox) childRow.getChildAt(3);
            if (childSelect == null)
            {
                Log.d(DETAILTAG, "Is null for some reason...");
                break;
            }
            rowid = (Integer) childSelect.getTag(R.id.tagRowid);
            Log.d(DETAILTAG, "Child's rowid: " + rowid);
            Log.d(DETAILTAG, "Selected integers: " + selectedIntegers[m]);
            if (rowid == null)
            {
                Log.d(DETAILTAG, "It is null....");
                break;
            }
            if (rowid.equals(selectedIntegers[m]))
            {
                //Delete this row and move onto the next integer
                Log.d(DETAILTAG, "Deleting the row");
                detailTable.removeViewAt(k);
                m++;
                k--;
            }
        }
        //Change the home page if they delete something and press the back button
        MainActivity.redo = true;
        MainActivity.modified = categoryName;
        //Clean selected rows
        selectedRows.clear();
        //Update the selectedTotal textView
        selectedTotal = Float.valueOf(0);
        TextView selTotView = findViewById(R.id.selectedTotal);
        selTotView.setText("0");
        //Make sure to vacuum when this page is closed
        needsVacuum = true;
        //Make a snack to notify the user
        Snackbar delCompleteSnack = Snackbar.make(
                findViewById(R.id.moreDetailsTitle),
                "Deletion complete",
                Snackbar.LENGTH_SHORT
        );
        delCompleteSnack.show();
    }

    @Override
    public void onDelSelNegativeClick(DialogFragment dialog)
    {
        Snackbar dontDelSnack = Snackbar.make(
                findViewById(R.id.moreDetailsTitle),
                "Deletion cancelled",
                Snackbar.LENGTH_SHORT
        );
        dontDelSnack.show();
    }

    public void deleteAll (View v)
    {
        //Make a dialog prompting the user for a response
        DialogFragment dialog = new deleteAllDialog();
        dialog.show(getFragmentManager(), "Delete all");
    }

    @Override
    public void onDelAllPositiveClick(DialogFragment dialog)
    {
        //Delete the table associated with this category, raise a confirmation first
        MainActivity.maindb.execSQL(
                "DROP TABLE IF EXISTS " + categoryName + "table;"
        );
        //Remove from the main table the associated table
        MainActivity.maindb.delete(
                databaseHelper.tableName,
                databaseHelper.dbcol1+"=?",
                new String[] {categoryName}
        );
        //Make sure to vacuum later
        needsVacuum = true;
        Log.d(DETAILTAG, "Deletion complete");
        //Make a snack to tell the user of the deletion
        Snackbar delAllSnack = Snackbar.make(
                findViewById(R.id.moreDetailsTitle),
                "Category deleted.",
                Snackbar.LENGTH_SHORT
        );
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDelAllNegativeClick(DialogFragment dialog)
    {
        //Make a snack to tell the user of the cancellation
        Snackbar dontDelSnack = Snackbar.make(
                findViewById(R.id.moreDetailsTitle),
                "Deletion cancelled",
                Snackbar.LENGTH_SHORT
        );
        dontDelSnack.show();
    }

    public void editSelected(View v)
    {
        //Edit the selected row(s)
        if (selectedRows.isEmpty())
        {
            Log.d(DETAILTAG, "Empty");
            Snackbar emptySnack = Snackbar.make(findViewById(R.id.moreDetailsTitle),
                    "No rows are selected", Snackbar.LENGTH_SHORT);
            emptySnack.show();
        }
        else if (selectedRows.size() > 1)
        {
            Log.d(DETAILTAG, "Selected more than 1");
            Snackbar tooMuchSnacks = Snackbar.make(findViewById(R.id.moreDetailsTitle),
                    "Please select 1 row", Snackbar.LENGTH_SHORT);
            tooMuchSnacks.show();
        }
        else
        {
            Intent newIntent = new Intent(this, editRows.class);
            newIntent.putExtra("category", categoryName);
            newIntent.putExtra("selectedRow", selectedRows.get(0));
            startActivity(newIntent);
        }
    }

}