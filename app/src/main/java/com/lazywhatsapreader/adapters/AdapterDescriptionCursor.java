package com.lazywhatsapreader.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.lazywhatsapreader.R;
import com.lazywhatsapreader.common.CommonUtilities;
import com.lazywhatsapreader.common.DatabaseHandler;

/**
 * Created by Gopinath on 5/16/2017.
 */

public class AdapterDescriptionCursor extends CursorAdapter {

    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public AdapterDescriptionCursor(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mLayoutInflater.from(context).inflate(R.layout.messagedescriptionsingle, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView one = view.findViewById(R.id.textView1);
        TextView two = view.findViewById(R.id.textView2);
        TextView textView_words = view.findViewById(R.id.textView_words);
        one.setText(cursor.getPosition() + 1 + ".    " + cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.MESSAGE_COLUMN_TIME)));
        textView_words.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.MESSAGE_COLUMN_MSG3)));

        String countString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.MESSAGE_COLUMN_MSG3));
        int wordcounts = countString.split("\\s+").length;
        textView_words.setText(wordcounts + " words");

        if (CommonUtilities.playposition == cursor.getPosition()) {

            two.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.MESSAGE_COLUMN_MSG3))));
        } else {
            two.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.MESSAGE_COLUMN_MSG3)));
        }

        if (Integer.parseInt(cursor.getString(6)) == 0) {
            // two.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            two.setTypeface(null, Typeface.BOLD);
        } else {
            // two.setTextColor(ContextCompat.getColor(context, R.color.black));
            two.setTypeface(null, Typeface.NORMAL);
        }
       /* if(cursor.getPosition()== SpeechMainActivity.cursor.getPosition()){
            two.setBackgroundColor(Color.parseColor("#eeeeee"));
        }
        else {
            //two.setBackgroundResource(R.drawable.shadowwhite);
            two.setBackgroundColor(Color.parseColor("#ffffff"));
        }*/

    }
}
