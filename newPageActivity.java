package com.example.davidtruong.list;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import java.util.Iterator;
import java.util.Set;

public class newPageActivity extends AppCompatActivity implements deletePageDialogFragment.deletePageListener{

    public static final String PAGETAG = "Page_activity";
    String selectedPage;
    Set<String> databaseNames;
    static Float grandTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.pageToolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Page Viewer");
        ab.setSubtitle(MainActivity.databaseResources.getString(getString(R.string.current_name), "Empty"));
        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.page)));
        //Get the names of databases for filling the table
        databaseNames = MainActivity.databaseResources.getStringSet(getString(R.string.database_names), null);
        if (databaseNames == null)
        {
            Log.d(PAGETAG, "Null for some reason");
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }
        Iterator<String> iter = databaseNames.iterator();
        //Set the table's properties
        TableLayout pageTable = findViewById(R.id.pageTable);
        pageTable.setStretchAllColumns(true);
        pageTable.setGravity(Gravity.LEFT);
        //Add the page rows to the table
        String pageName;
        while (iter.hasNext())
        {
            pageName = iter.next();
            addRow(pageName);
        }
    }

    public void addPage(View v)
    {
        //Add a page
        EditText newPageView = findViewById(R.id.newPageName);
        String pageName = newPageView.getText().toString();
        if (!MainActivity.stringParse(pageName))
        {
            snackPage("Please use letters and digits only as the page name");
            return;
        }
        if (databaseNames == null)
        {
            Log.d(PAGETAG, "Null for some reason");
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }
        databaseNames.add(pageName);
        SharedPreferences.Editor editor = MainActivity.databaseResources.edit();
        editor.putStringSet(getString(R.string.database_names), databaseNames);
        editor.apply();
        addRow(pageName);
        snackPage("Page added successfully");
    }

    public void deletePage(View v)
    {
        selectedPage = (String) v.getTag();
        DialogFragment dialog = new deletePageDialogFragment();
        dialog.show(getFragmentManager(), "Delete page fragment");
    }

    public void onDelPagePosClick(DialogFragment dialog)
    {
        //Delete the database associated with that page
        String currPage = MainActivity.databaseResources.getString("current_name", null);
        if (selectedPage.equals(currPage))
        {
            snackPage("Cannot close page you are currently using.");
            return;
        }
        if (this.deleteDatabase(selectedPage))
        {
            Log.d(PAGETAG, "Database successfully deleted");
        }
        else
        {
            Log.d(PAGETAG, "Database not deleted.");
        }
        //Find the row in the table and delete it
        TableLayout pageTable = findViewById(R.id.pageTable);
        for (Integer i = 1; i < pageTable.getChildCount(); i++)
        {
            TableRow currRow = (TableRow) pageTable.getChildAt(i);
            TextView currView = (TextView) currRow.getChildAt(0);
            String currPageName = currView.getText().toString();
            if (selectedPage.equals(currPageName))
            {
                //If the selected page has the same page name, then we are at the right spot
                pageTable.removeViewAt(i);
                break;
            }
        }
        //Delete the name from the string set and update the shared preference
        databaseNames.remove(selectedPage);
        SharedPreferences.Editor editor = MainActivity.databaseResources.edit();
        editor.putStringSet(getString(R.string.database_names), databaseNames);
        editor.apply();
        snackPage("Page deleted successfully.");
    }

    public void onDelPageNegClick(DialogFragment dialog)
    {
        Log.d(PAGETAG, "No was pressed. Do nothing");
        snackPage("Page deletion cancelled.");
    }

    public void onLoad(View v)
    {
        //Get the page name from the view
        selectedPage = (String) v.getTag();
        //Close the current database to prep for a new database load
        String currName = MainActivity.databaseResources.getString(getString(R.string.current_name), null);
        if (currName == null)
        {
            Log.d(PAGETAG, "Error with getting current name");
            return;
        }
        if (!currName.equals(selectedPage))
        {
            //Close the database and change current name
            MainActivity.maindb.close();
            Log.d(PAGETAG, "Succesfully closed");
            SharedPreferences.Editor editor = MainActivity.databaseResources.edit();
            editor.putString(getString(R.string.current_name), selectedPage);
            editor.apply();
            //Change these values to properly set up the database load
            databaseHelper.DATABASE_NAME = selectedPage;
            MainActivity.reload = true;
        }
        snackPage("Page successfully loaded");
    }


    public void addRow(String pageName)
    {
        //Add a row to the main table of this page
        TableLayout pageTable = findViewById(R.id.pageTable);
        //Make the components for the row
        TableRow currRow = new TableRow(this);
        TextView pageView = new TextView(this);
        Button loadBut = new Button(this);
        Button deleteBut = new Button(this);

        //Set their properties
        MainActivity.setTextView(pageView, pageName, Color.BLACK);
        MainActivity.setButton(loadBut, "Load", Color.BLACK);
        MainActivity.setButton(deleteBut, "Delete", Color.BLACK);
        loadBut.setTag(pageName);
        deleteBut.setTag(pageName);

        //Set the button's functions
        loadBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Load up the proper database and then go back to main page
                Log.d(PAGETAG, "Load button pressed");
                onLoad(view);
            }
        });

        deleteBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Make a dialog confirming the deletion of the page
                Log.d(PAGETAG, "Delete button pressed");
                deletePage(view);
            }
        });

        //Now add them to the row and add them to table
        currRow.addView(pageView);
        currRow.addView(loadBut);
        currRow.addView(deleteBut);
        pageTable.addView(currRow);
    }

    public void snackPage(String notification)
    {
        //Use this to display a Snackbar message
        //Hide the keyboard
        TextView view = findViewById(R.id.pageTitle);
        InputMethodManager keyHider = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyHider.hideSoftInputFromWindow(view.getWindowToken(), 0);
        Snackbar errorSnack = Snackbar.make(findViewById(R.id.pageTitle),
                notification, Snackbar.LENGTH_SHORT);
        errorSnack.show();
    }

}
