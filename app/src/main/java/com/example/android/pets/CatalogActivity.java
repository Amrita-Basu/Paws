/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetCursorAdapter;

import org.w3c.dom.Text;

import java.util.List;
import java.util.logging.Logger;

import static android.R.attr.dial;
import static android.R.attr.y;
import static com.example.android.pets.R.id.fab;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    ListView listView;
    PetCursorAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        listView = (ListView) findViewById(R.id.list_view_pet);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        adapter = new PetCursorAdapter(this, null);
        listView.setAdapter(adapter);

        if (adapter.isEmpty()) {
            invalidateOptionsMenu();
        }

        getLoaderManager().initLoader(0, null, this);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                Uri currentPetUri = ContentUris.withAppendedId(PetContract.PetEntry.CONTENT_URI, id);
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.setData(currentPetUri);
                startActivity(intent);

            }
        });


        Log.e("Hehe", "On Create");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.e("Hehe", "On create loader");
        String[] projection = new String[]{PetContract.PetEntry._ID, PetContract.PetEntry.COLUMN_PET_NAME, PetContract.PetEntry.COLUMN_PET_BREED};

        return new CursorLoader(this, PetContract.PetEntry.CONTENT_URI, projection, null, null, null);


    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.e("Hehe", "On load finished");
        adapter.swapCursor(data);

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        adapter.swapCursor(null);
        Log.e("Hehe", "On Loader reset");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //displayDatabaseInfo();
        Log.e("Hehe", "On Resume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Hehe", "On destroy");
    }

    /* @Override
    protected void onStop() {
        super.onStop();
        cursor.close();
    }*/

    /*private void displayDatabaseInfo() {

        // String[] projection = {PetContract.PetEntry._ID, PetContract.PetEntry.COLUMN_PET_NAME};
        //String selection = PetContract.PetEntry.COLUMN_PET_GENDER + "=?";
        //String [] selectionArgs = {String.valueOf(PetContract.PetEntry.GENDER_FEMALE)};

        cursor = getContentResolver().query(PetContract.PetEntry.CONTENT_URI, null, null, null, null);
        //Toast.makeText(this, "Is cursor pointing to the row before the first row? "+ cursor.isBeforeFirst(), Toast.LENGTH_SHORT).show();


        PetCursorAdapter adapter = new PetCursorAdapter(this, cursor);
        listView.setAdapter(adapter);


    }*/

    private void insertDummyPet() {

        ContentValues values = new ContentValues();
        values.put(PetContract.PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetContract.PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetContract.PetEntry.COLUMN_PET_GENDER, PetContract.PetEntry.GENDER_MALE);
        values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, 7);

        Uri uri = getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, values);

        if (uri == null) {
            Toast.makeText(this, getString(R.string.editor_insert_pet_failure), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You just added a dummy pet!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteAllPets = menu.findItem(R.id.action_delete_all_entries);
        if (adapter.isEmpty()) {
            deleteAllPets.setVisible(false);
        } else {
            deleteAllPets.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteAllPetDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showDeleteAllPetDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all pets? This action can not be reverted");
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        builder.setNegativeButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllPets();
            }
        });


        AlertDialog alert = builder.create();
        alert.show();

    }


    private void deleteAllPets() {

        int rowNum = getContentResolver().delete(PetContract.PetEntry.CONTENT_URI, null, null);
        if (rowNum > 0) {
            Toast.makeText(this, "All pets have been deleted!", Toast.LENGTH_LONG);
        } else {
            Toast.makeText(this, "Error deleting pets!", Toast.LENGTH_LONG);
        }


    }


}
