package com.lazywhatsapreader.activities;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.lazywhatsapreader.R;
import com.lazywhatsapreader.adapters.AdapterDescriptionCursor;
import com.lazywhatsapreader.listeners.SpeechCompleteListener;
import com.lazywhatsapreader.session.SharedPreference;

public class MessageDescriptionActivity extends AppCompatActivity implements SpeechCompleteListener {
    ListView listViewdescription;
    AdapterDescriptionCursor adapter;
    private SharedPreference sh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.description_layout);
        sh = SharedPreference.getInstance(this);
        listViewdescription = (ListView) findViewById(R.id.listViewdescription);
        adapter = new AdapterDescriptionCursor(this, SpeechMainActivity.cursor, true);
        listViewdescription.setAdapter(adapter);
        if (getIntent().getBooleanExtra("havePosition", false)) {
            listViewdescription.setSelection(sh.getLastPlayedPosition());
            int h1 = listViewdescription.getHeight();
            listViewdescription.smoothScrollToPositionFromTop(sh.getLastPlayedPosition(), h1, 2000);
        }
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public void completed(String a) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
