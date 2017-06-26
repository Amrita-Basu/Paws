package com.example.android.pets.data;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.R;

/**
 * Created by AMRITA BASU on 23-06-2017.
 */

public class PetCursorAdapter extends CursorAdapter {


    public PetCursorAdapter(Context context, Cursor cursor) {


        super(context, cursor, 0);

    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {


        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);


    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {


        TextView name = (TextView) view.findViewById(R.id.name);
        TextView summary = (TextView) view.findViewById(R.id.summary);

        String petName = cursor.getString(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME));
        String petSummary = cursor.getString(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED));

        name.setText(petName);

        if (TextUtils.isEmpty(petSummary)) {
            summary.setText("Unknown breed");
        } else {
            summary.setText(petSummary);
        }


    }
}
