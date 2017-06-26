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
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetsDbHelper;

import java.util.IllegalFormatException;

import static android.R.attr.id;
import static android.R.attr.name;
import static java.security.AccessController.getContext;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    private boolean petHasChanged = false;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            petHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();

        mNameEditText.setOnTouchListener(touchListener);
        mBreedEditText.setOnTouchListener(touchListener);
        mGenderSpinner.setOnTouchListener(touchListener);
        mWeightEditText.setOnTouchListener(touchListener);

        Intent intent = getIntent();
        Uri currentPetUri = intent.getData();

        if (currentPetUri == null) {
            setTitle("Add a Pet");
            invalidateOptionsMenu();
        } else {
            setTitle("Edit Pet");
            getLoaderManager().initLoader(1, null, this);

        }


    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (getIntent().getData() == null) {

            MenuItem delete = menu.findItem(R.id.action_delete);
            delete.setVisible(false);
        }

        return true;
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("KEEP EDITING", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }

            }
        });

        builder.setNegativeButton("DISCARD", discardButtonClickListener);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetContract.PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetContract.PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetContract.PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }


    private void savePet() {

        try {
            Uri currentPetUri = getIntent().getData();

            String name = mNameEditText.getText().toString().trim();
            String breed = mBreedEditText.getText().toString().trim();
            Integer gender = mGender;

            String weightValue = mWeightEditText.getText().toString().trim();
            Integer weight = 0;
            if (!TextUtils.isEmpty(weightValue)) {
                weight = Integer.parseInt(weightValue);
            }
            ContentValues values = new ContentValues();
            values.put(PetContract.PetEntry.COLUMN_PET_NAME, name);
            values.put(PetContract.PetEntry.COLUMN_PET_BREED, breed);
            values.put(PetContract.PetEntry.COLUMN_PET_GENDER, mGender);
            values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, weight);

            if (currentPetUri == null) {

                Uri uri = getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, values);
                if (uri == null) {
                    Toast.makeText(this, getString(R.string.editor_insert_pet_failure), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_insert_pet_successful) , Toast.LENGTH_SHORT).show();
                }
            } else {
                if (petHasChanged) {
                    int rowNum = getContentResolver().update(currentPetUri, values, null, null);
                    if (rowNum <= 0) {
                        Toast.makeText(this, getString(R.string.editor_update_pet_failure), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, getString(R.string.editor_update_pet_successful), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No changes made to this pet!", Toast.LENGTH_SHORT).show();
                }
            }


            finish();


        } catch (NullPointerException e) {
            Toast.makeText(this, "Please enter a value for name", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a numeric value greater than or equal to zero for weight", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Insertion is not supported for: " + PetContract.PetEntry.CONTENT_URI, Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    public void onBackPressed() {
        if (!petHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditorActivity.super.onBackPressed();
               // finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if (!petHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this pet?");
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 deletePet();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    private void deletePet(){

        int rowNum = getContentResolver().delete(getIntent().getData(), null, null);
        if (rowNum == 1) {
            Toast.makeText(this, "Pet removed successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Pet could not be removed!", Toast.LENGTH_SHORT).show();
        }
        finish();

    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        Uri uri = getIntent().getData(); // it wont be null coz loading is initiated in onCreate() only when uri is not null
        Log.e("Hehe", "Editor: onCreateLoader");
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        // Moving to 0th position:
        while (data.moveToNext()) { // its gonna have just one row anyway
            data.moveToFirst();

            String name = data.getString(data.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME));
            // if(data.getCount()==)
            String breed = data.getString(data.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED));
            int gender = data.getInt(data.getColumnIndex(PetContract.PetEntry.COLUMN_PET_GENDER));
            int weight = data.getInt(data.getColumnIndex(PetContract.PetEntry.COLUMN_PET_WEIGHT));

            Log.e("Hehe", "Editor: onLoadFinished");
            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mGenderSpinner.setSelection(gender);
            mWeightEditText.setText(Integer.toString(weight));
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

        mNameEditText.setText("");
        mBreedEditText.setText("");
        mGenderSpinner.setSelection(PetContract.PetEntry.GENDER_UNKNOWN);
        mWeightEditText.setText("");
        Log.e("Hehe", "Editor: onLoaderReset");

    }
}