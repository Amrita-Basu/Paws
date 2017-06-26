package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DecorContentParent;
import android.util.Log;
import android.widget.Toast;

import static android.R.attr.id;

/**
 * Created by AMRITA BASU on 10-06-2017.
 */

public class PetProvider extends ContentProvider {

    PetsDbHelper mDbHelper;
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    private static final int PETS = 100;
    private static final int PET_ID = 101;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        uriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);

        uriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);


    }

    @Override
    public boolean onCreate() {

        mDbHelper = new PetsDbHelper(getContext());
        return true;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {

        int match = uriMatcher.match(uri);
        switch (match) {

            case PETS:
                return insertPet(uri, values);

            default:
                throw new IllegalArgumentException("Insertion is not supported for: " + uri);


        }

    }

    private Uri insertPet(Uri uri, ContentValues values) throws NullPointerException, IllegalArgumentException {
        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if (name == null || name.length() == 0) {
            throw new NullPointerException();
        }

        int weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);

        if (weight < 0) {
            throw new NumberFormatException();
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insert(PetContract.PetEntry.TABLE_NAME, null, values);

        if (rowId == -1) {
            Log.e(LOG_TAG, "Failed to insert pet for: " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri , null);

        return ContentUris.withAppendedId(uri, rowId);

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int match = uriMatcher.match(uri);

        switch (match) {
            case PETS:
                return updatePet(uri, values, selection, selectionArgs);

            case PET_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for: " + uri);


        }


    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size() == 0) {
            return 0;
        }

        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_NAME) && (values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME) == null || values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME).length() == 0)) {
            throw new NullPointerException();
        }

        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT) && values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT) <0) {

            throw new NumberFormatException();
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsNum = db.update(PetContract.PetEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsNum <= 0) {
            Log.e(LOG_TAG, "Update was unsuccessful!");
            return 0;

        }
         else{
        getContext().getContentResolver().notifyChange(uri, null);}
        return rowsNum;

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = uriMatcher.match(uri);
        switch (match) {

            case PETS:

                cursor = db.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case PET_ID:

                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:

                throw new IllegalArgumentException("No match found for uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowNum = 0;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);

        switch (match) {

            case PETS:

                rowNum =  db.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
                if(rowNum > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowNum;


            case PET_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowNum = db.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
                if(rowNum > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowNum;

            default:
                throw new IllegalArgumentException("Deletion is not supported for this uri: " + uri);

        }






    }


    @Override
    public String getType(Uri uri) {

        int match = uriMatcher.match(uri);

        switch (match) {

            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri " + uri + " with match " + match);

        }
    }
}
