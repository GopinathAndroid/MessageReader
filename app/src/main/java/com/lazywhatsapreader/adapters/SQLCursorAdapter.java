package com.lazywhatsapreader.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.lazywhatsapreader.R;
import com.lazywhatsapreader.common.DatabaseHandler;

/**
 * Created by Gopinath on 5/16/2017.
 */

public class SQLCursorAdapter extends CursorAdapter {

    private SparseBooleanArray mSelectedItemsIds;

    public SQLCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.singleview_new, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // final int position=cursor.getPosition();

        TextView content = view.findViewById(R.id.content);
        TextView title = view.findViewById(R.id.title);
        TextView wordcount =  view.findViewById(R.id.wordsize);
        // LinearLayout parent_view_background = (LinearLayout) view.findViewById(R.id.parent_view_background);
        String contentString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.MESSAGE_COLUMN_MSG4));
        String countString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.MESSAGE_COLUMN_MSG3));
        int wordcounts = countString.split("\\s+").length;
        content.setText(contentString);
        wordcount.setText(wordcounts + " words");
        String titleText=getCursor().getPosition() + 1 + ".   " + cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.MESSAGE_COLUMN_TIME));
        title.setText(titleText);

        if (Integer.parseInt(cursor.getString(6)) == 0) {
            content.setTypeface(null, Typeface.BOLD);
            //   content.setTextColor(ContextCompat.getColor(context, R.color.black));
            //   parent_view_background.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        } else {

            //   content.setTextColor(ContextCompat.getColor(context, R.color.grey));
            //   parent_view_background.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            content.setTypeface(null, Typeface.NORMAL);
        }


    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}
