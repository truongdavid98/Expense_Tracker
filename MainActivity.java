package com.example.davidtruong.list;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

//Opening screen of the app
public class MainActivity extends AppCompatActivity{
    public static final String TABLETAG = "Table_main";
    public static final String DBTAG = "Database_main";
    public static final String STARTUPTAG = "Startup_main";
    public static final String MENUTAG = "Menu_main";

    public static SQLiteDatabase maindb;
    public static boolean startup = true;
    public static boolean reload = true;
    public static SharedPreferences databaseResources;
    public static boolean redo = false;
    public static String modified;
    private DrawerLayout mDrawerLayout;
    static Float pageTotal;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(STARTUPTAG, "On create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("Home page");
        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.home)));
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        //Set up navigation bar on the left side
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId())
                        {
                            case R.id.page_viewer:
                                Log.d(MENUTAG, "Create page was selected sucessfully");
                                Intent intent = new Intent(MainActivity.this, newPageActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.calender_view:
                                Log.d(MENUTAG, "Calender selected");
                                Intent calenderIntent = new Intent(MainActivity.this, calenderDetails.class);
                                startActivity(calenderIntent);
                                return true;
                            case R.id.chart_view:
                                Log.d(MENUTAG, "Chart selected");
                                Intent chartIntent = new Intent(MainActivity.this, graphPercentages.class);
                                startActivity(chartIntent);
                                return true;
                            default:
                                Log.d(MENUTAG, "Default reached");
                                return true;
                        }
                    }
                });
        //Do these startup functions once in the beginning
        if (startup) {
            startup = false;
            Log.d(DBTAG, "Get the database and the shared preferences");
            databaseResources = this.getSharedPreferences(
                    getString(R.string.database_resources),
                    Context.MODE_PRIVATE
            );
            String currName = databaseResources.getString(getString(R.string.current_name), null);
            if (currName == null)
            {
                databaseHelper.DATABASE_NAME = "Maindatabase";
                Set<String> dbNames = databaseResources.getStringSet(getString(R.string.database_names), new HashSet<String>());
                SharedPreferences.Editor editor = databaseResources.edit();
                if (dbNames.add("Maindatabase")) {
                    editor.putStringSet("database_names", dbNames);
                }
                currName="Maindatabase";
                editor.putString("current_name", currName);
                editor.apply();
            }
            else
            {
                databaseHelper.DATABASE_NAME = currName;
            }
        }
        if (reload)
        {
            //Reload the database
            Log.d(DBTAG, "Reloading database");
            ab.setSubtitle(databaseHelper.DATABASE_NAME);
            databaseHelper dbHelper = new databaseHelper(this);
            maindb = dbHelper.getWritableDatabase();
        }
        //Get all the entries, and make the table
        Cursor allEntries = maindb.query(
                databaseHelper.tableName,
                null,
                null,
                null,
                null,
                null,
                null
        );
        String category;
        Float amt;
        pageTotal = Float.valueOf(0);
        String categoryDisplay;
        while (allEntries.moveToNext())
        {
            //Make the corresponding rows for the categories
            category = allEntries.getString(allEntries.getColumnIndex(databaseHelper.dbcol1));
            amt = allEntries.getFloat(allEntries.getColumnIndex(databaseHelper.dbcol2));
            pageTotal += amt;
            categoryDisplay = category.trim().replace('_', ' ').toLowerCase();
            mainRowBuilder(categoryDisplay, amt);
        }
        Log.d(STARTUPTAG, "Main table made.");
        if (redo) {
            //Set to false because onCreate() naturally takes care of redo's job
            redo = false;
        }
        allEntries.close();
        setPageTotal();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(STARTUPTAG, "On resume");
        if (redo)
        {
            //Update the main table's values
            //Get the values from the database
            Cursor values = maindb.query(
                    databaseHelper.tableName,
                    null,
                    databaseHelper.dbcol1 + "=?",
                    new String[] {modified},
                    null,
                    null,
                    null
            );
            values.moveToNext();
            Float newTotal = values.getFloat(values.getColumnIndex(databaseHelper.dbcol2));
            //Go through the main table and find the rows we need to modify
            TableLayout mainTable = findViewById(R.id.tableInput);
            TableRow currRow;
            TextView currCat;
            TextView currTot;
            String catName;
            Float oldValue;
            String holder;
            for (Integer i = 1; i < mainTable.getChildCount(); i++)
            {
                currRow = (TableRow) mainTable.getChildAt(i);
                currCat = (TextView) currRow.getChildAt(0);
                catName = currCat.getText().toString();
                if (modified.equals(catName))
                {
                    //Change the value and end the function
                    currTot = (TextView) currRow.getChildAt(1);
                    holder = currTot.getText().toString();
                    oldValue = Float.parseFloat(holder);
                    //Update the page total
                    pageTotal -= oldValue;
                    pageTotal += newTotal;
                    setPageTotal();
                    currTot.setText(newTotal.toString());
                    redo = false;
                    return;
                }
            }
        }
    }

    public void updateTable (View view)
    {
        //Grab the category name from the user
        EditText inputCat = findViewById(R.id.catEntry);
        final String category = inputCat.getText().toString();
        if (category.isEmpty())
        {
            snackNotify("Please insert a category name");
            return;
        }
        if (!stringParse(category))
        {
            snackNotify("Please use only letters or numbers for category name");
            return;
        }
        Log.d(TABLETAG, "String is: " + category);

        //Grab the amount value
        EditText inputAmt = (EditText) findViewById(R.id.amtEntry);
        String amtText = inputAmt.getText().toString();
        if (amtText.isEmpty())
        {
            snackNotify("Please insert an amount");
            return;
        }
        String formattedAmt = amtParse(amtText);
        if(formattedAmt == null)
        {
            snackNotify("Please use a valid number format for the amount");
            return;
        }
        Float amt = Float.parseFloat(formattedAmt);
        if (view == findViewById(R.id.minusEntry))
        {
            //If - was pressed, make the value negative
            amt *= -1;
        }
        //Update the page total
        pageTotal += amt;
        setPageTotal();
        Log.d(DBTAG, "Amt is: " + amt);

        //Grab the detail string
        EditText inputDetail = findViewById(R.id.detailEntry);
        String detail = inputDetail.getText().toString();

        //Put the information into Value to pass to sqlite, use a modified name for the category
        ContentValues value = new ContentValues();
        String tableName = category.trim().replace(' ', '_').toLowerCase();
        value.put(databaseHelper.dbcol1, tableName);
        value.put(databaseHelper.dbcol2, amt);

        //Insert the row information
        Log.d(TABLETAG, "Insert new row of name: " + tableName);
        long newRowId = maindb.insert("MainTable", null, value);
        Log.d(TABLETAG, "Insertion done. Row id = " + newRowId);

        if (newRowId != -1)
        {
            //If it is a new category, make the underlying table for the new category
            Log.d(DBTAG, "Add to database the underlying table for the new database");
            Log.d(DBTAG, "Table name is: " + tableName);
            maindb.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + tableName + "table ( " +
                            "Detail text," +
                            "Amount Integer NOT NULL," +
                            "Date text DEFAULT CURRENT_DATE);"
            );
            //Make insert trigger for underlying table
            Log.d(DBTAG, "Make triggers for the underlying table");
            maindb.execSQL(
                    "CREATE TRIGGER " + tableName + "_insertTrigger " +
                            "AFTER INSERT ON " + tableName + "table " +
                            "BEGIN " +
                            "UPDATE " + databaseHelper.tableName +
                            " SET " + databaseHelper.dbcol2 + "=(Select SUM(Amount) FROM " + tableName + "table) " +
                            "WHERE " + databaseHelper.dbcol1 + "='" + tableName + "'; " +
                            "END"

            );
            //Make delete trigger for underlying table
            maindb.execSQL(
                    "CREATE TRIGGER " + tableName + "_deleteTrigger " +
                            "AFTER DELETE ON " + tableName + "table " +
                            "BEGIN " +
                            "UPDATE " + databaseHelper.tableName +
                            " SET " + databaseHelper.dbcol2 + "=(Select SUM(Amount) FROM " + tableName + "table) " +
                            "WHERE " + databaseHelper.dbcol1 + "='" + tableName + "'; " +
                            "END"

            );
            //Make update trigger for underlying table
            maindb.execSQL(
                    "CREATE TRIGGER " + tableName + "_updateTrigger " +
                            "AFTER UPDATE ON " + tableName + "table " +
                            "BEGIN " +
                            "UPDATE " + databaseHelper.tableName +
                            " SET " + databaseHelper.dbcol2 + "=(Select SUM(Amount) FROM " + tableName + "table) " +
                            "WHERE " + databaseHelper.dbcol1 + "='" + tableName + "'; " +
                            "END"

            );
            //Add a new row to the table
            mainRowBuilder(category, amt);
        }

        //Now add the entry to the underlying table
        ContentValues entryValues = new ContentValues();
        entryValues.put("Detail", detail);
        entryValues.put("Amount", amt);

        Log.d(DBTAG, "Add the entry to the underlying table");
        maindb.insert(tableName + "table", null, entryValues);
        Log.d(DBTAG, "Insertion successful");

        if (newRowId == -1) {
            //Already exists so update the value in the main table
            Cursor info = maindb.query(
                    databaseHelper.tableName,
                    null,
                    databaseHelper.dbcol1 + "=?",
                    new String[] {category},
                    null,
                    null,
                    null
            );
            if (!info.moveToNext()) {
                //If the cursor doesn't have any input for some reason, put up an error
                Log.d(TABLETAG, "Cursor doesn't have any input?");
                snackNotify("Error: Cursor didn't find information.");
                info.close();
                return;
            }
            //Print out cursor info
            String cursorMsg = DatabaseUtils.dumpCursorToString(info);
            Log.d(DBTAG, cursorMsg);
            Float newTotal = info.getFloat(info.getColumnIndex(databaseHelper.dbcol2));
            Log.d(TABLETAG, "Total from cursor is: " + newTotal);
            String totalText = Float.toString(newTotal);
            Log.d(TABLETAG, "String of float is: " + totalText);

            //Find the view and fix the amount displayed
            TableLayout mainTable = findViewById(R.id.tableInput);
            Log.d(TABLETAG, "Table Name: " + tableName);
            String currStringHold;
            for (Integer i = 0; i < mainTable.getChildCount(); i++)
            {
                TableRow holder = (TableRow) mainTable.getChildAt(i);
                TextView currCat = (TextView) holder.getChildAt(0);
                currStringHold = currCat.getText().toString().replace(' ', '_').trim().toLowerCase();
                Log.d(TABLETAG, "Current textView: " + currCat.getText());
                if (currStringHold.equals(tableName))
                {
                    //We found the right category, so update the amount
                    Log.d(TABLETAG, "Found the right category child.");
                    TextView currAmtView = (TextView) holder.getChildAt(1);
                    Log.d(TABLETAG, "Intermediate");
                    currAmtView.setText(amtParse(totalText));
                    break;
                }
            }
            //Everything is finished, so clean up the resources and end the func
            snackNotify("Category exists. Entry added to preexisting table");
            info.close();
        }
        else {
            //Notify the user
            snackNotify("Entry successfully added");
        }
    }

    public static void setTextView(TextView view, String text, int color)
    {
        //Set the parameters for the TextViews that are a part of the table row
        view.setText(text);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    public static void setButton(Button button, String text, int color)
    {
        //Set the parameters for the buttons that are a part of the table row
        button.setText(text);
        button.setTextSize(12);
        button.setTextColor(color);
        button.setMinimumHeight(0);
        button.setMinHeight(0);
        button.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    public void snackNotify(String notification)
    {
        //Use this to display a Snackbar message
        hideKeyboard();
        Snackbar errorSnack = Snackbar.make(findViewById(R.id.mainTitle),
                notification, Snackbar.LENGTH_SHORT);
        errorSnack.show();
    }

    public void mainRowBuilder(String rowCatName, Float rowTotal)
    {
        final String category = rowCatName;
        //Create the TextViews
        Log.d(TABLETAG, "Make the TextViews and set their properties");
        TextView input1 = new TextView(this);
        TextView input2 = new TextView(this);
        //Use the function to set their attributes
        setTextView(input1, rowCatName, Color.BLACK);
        setTextView(input2, String.format(Locale.US, "%.2f", rowTotal), Color.BLACK);

        //Make buttons
        Log.d(TABLETAG, "Make the buttons and set their properties.");
        Button addButton = (Button) new Button(this);
        Button moreButton = (Button) new Button(this);

        //Set their properties
        setButton(addButton, "+", Color.BLACK);
        setButton(moreButton, "Info", Color.BLACK);

        //Set the onClick function for adding entry
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Code for the add button
                Intent addIntent = new Intent(MainActivity.this, addEntry.class);
                addIntent.putExtra("category", category.trim().replace(' ', '_').toLowerCase());
                startActivity(addIntent);
            }
        });

        //Set the onClick function for more entry
        moreButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Code for the add button
                Intent moreIntent = new Intent(MainActivity.this, moreDetails.class);
                moreIntent.putExtra("category", category.trim().replace(' ', '_').toLowerCase());
                startActivity(moreIntent);
            }
        });

        //Add the views into the table row
        TableRow newRow = new TableRow(this);
        newRow.addView(input1);
        newRow.addView(input2);
        newRow.addView(addButton);
        newRow.addView(moreButton);

        //Finally add the row into the array and into the table
        TableLayout mainTable = findViewById(R.id.tableInput);
        mainTable.setGravity(Gravity.LEFT);
        mainTable.addView(newRow);
        Log.d(STARTUPTAG, "Row made and added to table");
    }

    public void hideKeyboard()
    {
        //Hide the keyboard
        TextView view = findViewById(R.id.mainTitle);
        InputMethodManager keyHider = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyHider.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        //Handle item selection
        switch (item.getItemId())
        {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.page_viewer:
                Log.d(MENUTAG, "Create page was selected sucessfully");
                Intent intent = new Intent(this, newPageActivity.class);
                startActivity(intent);
                return true;
            case R.id.calender_view:
                Log.d(MENUTAG, "Calender selected");
                Intent calenderIntent = new Intent(this, calenderDetails.class);
                startActivity(calenderIntent);
                return true;
            default:
                Log.d(MENUTAG, "Default reached");
                return super.onOptionsItemSelected(item);
        }
    }

    public static boolean stringParse(String input)
    {
        //Make sure that the string passed in only consists of letters or digits or spaces
        for (Integer i = 0; i < input.length(); i++)
        {
            if (!Character.isLetter(input.charAt(i)))
            {
                if (!Character.isDigit(input.charAt(i)))
                    if (input.charAt(i) != ' ')
                        return false;
            }
        }
        return true;
    }

    public static String amtParse(String input)
    {
        //Make sure the amt passed in follows money format
        Log.d(TABLETAG, "Starting input is: " + input);
        Boolean decimalFlag = false;
        Integer counter = 0;
        for (Integer i = 0; i < input.length(); i++)
        {
            if(input.charAt(i) == '.')
            {
                //Decimal is found
                if (decimalFlag)
                {
                    //Means there are two decimals
                    Log.d(TABLETAG, "Two decimals found; returning null");
                    return null;
                }
                decimalFlag = true;
            }
            else if (decimalFlag)
            {
                counter++;
            }
        }
        if (decimalFlag && !(counter.equals(1) || counter.equals(2)))
        {
            //Needs at least 1 number after the decimal

            Log.d(TABLETAG, "Parse failed; returning null");
            return null;
        }
        else
        {
            Float holder = Float.parseFloat(input);
            String hold = String.format(Locale.US, "%.2f", holder);
            Log.d(TABLETAG, "Amt parsed is : " + hold);
            return hold;
        }

    }

    public void setPageTotal()
    {
        //Set the total
        TextView totalView = findViewById(R.id.pageTotalNumber);
        totalView.setText(String.format(Locale.US, "%.2f", pageTotal));
    }

}


