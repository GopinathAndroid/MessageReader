package com.lazywhatsapreader.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lazywhatsapreader.App;
import com.lazywhatsapreader.R;
import com.lazywhatsapreader.adapters.SQLCursorAdapter;
import com.lazywhatsapreader.com.lazywhatsappreader.reader.GranularTextToSpeech;
import com.lazywhatsapreader.com.lazywhatsappreader.reader.ReferencedHandler;
import com.lazywhatsapreader.common.ClipboardClass;
import com.lazywhatsapreader.common.CommonUtilities;
import com.lazywhatsapreader.common.DatabaseHandler;
import com.lazywhatsapreader.interfaces.DialogButtonClickCallBack;
import com.lazywhatsapreader.interfaces.UpdateInterface;
import com.lazywhatsapreader.utils.AppRater;
import com.lazywhatsapreader.utils.Dialogs;
import com.lazywhatsapreader.session.SharedPreference;
import com.lazywhatsapreader.slideup.SlidingUpPanelLayout;
import com.lazywhatsapreader.utils.ExitDialog;
import com.lazywhatsapreader.utils.Utility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;

public class SpeechMainActivity extends BaseActivity implements
        View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener
        , UpdateInterface, SeekBar.OnSeekBarChangeListener, AudioManager.OnAudioFocusChangeListener, DialogButtonClickCallBack {


    public static ProgressDialog progress;
    private GranularTextToSpeech mTtsWrapper;
    private TextToSpeech mTts;
    public static Locale mLocale;
    NativeAd adobj;
    /**
     * Handler used for transferring TTS callbacks to the main thread.
     */
    private final TypeAndSpeakHandler mHandler = new TypeAndSpeakHandler(this);

    LinearLayout colorLayout;
    private SharedPreference sh;

    private ListView listview1;
    private ClipboardClass clipobj;
    private DatabaseHandler db;
    SQLCursorAdapter cursorAdapter = null;
    private static final String TAG = "Panel";
    private EditText editText_highlightingtext;
    private FrameLayout adContainerView;
    private SlidingUpPanelLayout mLayout;
    private TextView textView_selected_message, textView_selected_message_position;
    private TextView textView_bottom_date;
    public static FloatingActionButton btn_player_play, btn_player_pause;
    private ImageButton btn_player_rewind, btn_player_forward, btn_player_next, btn_player_previous;
    private ImageButton btn_bottom_overflow, btn_equalizer, btn_bottom_language, btn_nightmode, btn_ttssettings, btn_activity;
    FloatingActionButton fab;
    CommonUtilities commonUtilities;
    SeekBar seekBarFontSize;
    ScrollView edt_scrollview;
    public static Cursor cursor = null;
    ///////////////////////////////////////////////////////
    private boolean callInterruptFlag = false;
    public static boolean isPaused = false;
    private long mLastClickTime = 0;
    SharedPreferences preference;
    ///////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar myToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, initializationStatus -> { });
        initViews();
        initListeners();

        loadBanner();
        loadNativeAd();

        db = DatabaseHandler.getInstance(this);
        sh = SharedPreference.getInstance(this);
        preference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        CommonUtilities.nightmode = preference.getBoolean("key_nightmode", false);

        updateSeekBar();
        clipobj = new ClipboardClass();
        clipobj.StartClipboard(SpeechMainActivity.this, preference);
        commonUtilities = new CommonUtilities(getApplicationContext());
        cursor = db.loadAllMessages();
        if (cursor.getCount() == 0) {
            sh.setLastPlayedPosition(0);
            clipobj.addFirstTimeMessageToTable(this);
            cursor = db.loadAllMessages();
        }
        cursorAdapter = new SQLCursorAdapter(this, cursor, true);
        listview1.setAdapter(cursorAdapter);
        // Set up text-to-speech.

        final TextToSpeech.OnInitListener initListener = status -> mHandler.transferOnTtsInitialized(status);
        setupCursorPosition();
        updatePullUpLayout();
        mTts = new TextToSpeech(this, initListener);
        mLocale = Locale.getDefault();

        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        listview1.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        listview1.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Capture total checked items
                final int checkedCount = listview1.getCheckedItemCount();
                // Set the CAB title according to total checked items
                mode.setTitle(checkedCount + " Selected");
                // Calls toggleSelection method from ListViewAdapter Class
                cursorAdapter.toggleSelection(position);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete) {// Calls getSelectedIds method from ListViewAdapter Class
                    SparseBooleanArray selected = cursorAdapter
                            .getSelectedIds();
                    if (mTtsWrapper != null && mTtsWrapper.isSpeaking()) {
                        mTtsWrapper.pause();
                    }
                    // Captures all selected ids with a loop
                    for (int i = (selected.size() - 1); i >= 0; i--) {
                        if (selected.valueAt(i)) {

                            Cursor mCursor = (Cursor) cursorAdapter.getItem(selected
                                    .keyAt(i));

                            int newId = mCursor.getInt(7);
                            db.deleteMessage(newId);

                        }
                    }
                    afterDelete();
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                cursorAdapter.removeSelection();
            }
        });


        listview1.setOnItemClickListener((parent, view, position, id) -> {
            // TODO Auto-generated method stub


            cursor.moveToPosition(position);

            if (mTtsWrapper != null && mTtsWrapper.isSpeaking()) {
                mTtsWrapper.setListener(null);
                mTtsWrapper.stop();

            }

            db.updateMessage(cursor.getInt(7));
            cursor = db.loadAllMessages();

            cursorAdapter.swapCursor(cursor);
            cursor.moveToPosition(position);
            sh.setLastPlayedPosition(cursor.getPosition());
            updatePullUpLayout();
            editText_highlightingtext.setSelection(0, 0);

            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        });


        listview1.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {


                if (scrollState == 0) {
                    fab.setVisibility(View.VISIBLE);
                    Log.i("a", "scrolling stopped...");
                } else {
                    fab.setVisibility(View.GONE);
                    Log.i("a", "scrolling starting...");
                }


            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
                if (newState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {

                    colorLayout.setVisibility(View.VISIBLE);
                    textView_bottom_date.setText("Language : " + mLocale.getDisplayName());

                } else if (newState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {

                    colorLayout.setVisibility(View.GONE);
                    textView_bottom_date.setText(cursor.getString(5));

                }

            }


        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onFade Clicked ");
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });



        try {
            AppRater.app_launched(this);

        }
        catch (Exception e){

        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            if(!sh.getIsFirstTime()) {
                clipobj.clipboardChanged(this);
                cursor = db.loadAllMessages();
                cursorAdapter.swapCursor(cursor);
                setupCursorPosition();
                setupNightMode();

            }
            sh.setIsFirstTime(false);
        }
    }

    private void initListeners() {
        fab.setOnClickListener(this);
        fab.setOnLongClickListener(this);
        btn_player_play.setOnClickListener(this);
        btn_player_pause.setOnClickListener(this);
        btn_bottom_language.setOnClickListener(this);
        btn_player_next.setOnClickListener(this);
        btn_player_rewind.setOnClickListener(this);
        btn_player_forward.setOnClickListener(this);
        btn_equalizer.setOnClickListener(this);
        btn_nightmode.setOnClickListener(this);
        btn_ttssettings.setOnClickListener(this);
        btn_activity.setOnClickListener(this);
        btn_player_previous.setOnClickListener(this);
        btn_bottom_overflow.setOnClickListener(this);
        seekBarFontSize.setOnSeekBarChangeListener(this);

        setupNightMode();
    }

    private void initViews() {
        textView_selected_message =  findViewById(R.id.textView_selected_message);
        textView_bottom_date = findViewById(R.id.textView_bottom_date);
        textView_selected_message_position =  findViewById(R.id.textView_selected_message_position);
        textView_selected_message.setSelected(true);
        adContainerView = findViewById(R.id.adContainerView);
        listview1 = findViewById(R.id.listView1);
        fab = findViewById(R.id.fab);
        editText_highlightingtext = findViewById(R.id.editText_highlightingtext);
        btn_player_play = findViewById(R.id.btn_player_play);
        btn_player_pause =  findViewById(R.id.btn_player_pause);
        btn_player_forward =  findViewById(R.id.btn_player_forward);
        btn_player_rewind = findViewById(R.id.btn_player_rewind);
        btn_player_next =  findViewById(R.id.btn_player_next);
        btn_bottom_language =  findViewById(R.id.btn_bottom_language);
        btn_equalizer =  findViewById(R.id.btn_equalizer);
        btn_nightmode = findViewById(R.id.btn_nightmode);
        btn_ttssettings = findViewById(R.id.btn_ttssettings);
        btn_activity =  findViewById(R.id.btn_activity);
        btn_bottom_overflow =  findViewById(R.id.btn_bottom_overflow);
        btn_player_previous =  findViewById(R.id.btn_player_previous);
        mLayout = findViewById(R.id.sliding_layout);
        seekBarFontSize =  findViewById(R.id.seekBarFontSize);
        colorLayout =  findViewById(R.id.colorLayout);
        edt_scrollview =  findViewById(R.id.edt_scrollview);
    }

    private void updateSeekBar() {
        try {
            seekBarFontSize.setProgress(0);
        } catch (Exception e) {

        }

        int max = 20;
        seekBarFontSize.setMax(max);
        seekBarFontSize.setProgress(sh.getTextSize());
    }

    /**
     * Speaks the current text aloud.
     */
    private void speak() {

        final CharSequence text = CommonUtilities.speakingText;

        if (TextUtils.isEmpty(text)) {
            //   mPinnedDialogManager.showPinnedDialog(PINNED_NO_TEXT, mSpeakButton);
            //  mHandler.dismissDialogDelayed(PINNED_NO_TEXT, 1000);
            return;
        }


        mTtsWrapper = new GranularTextToSpeech(this, mTts, mLocale, sh, this);
        mTtsWrapper.setListener(mSingAlongListener);
        mTtsWrapper.setLocale(mLocale);


        if (mLocale != null) {

            mTts.setLanguage(mLocale);
        }


        mTtsWrapper.setText(text);
        mTtsWrapper.setSegmentFromCursor(0);
        mTtsWrapper.speak();
        db.updateMessage(cursor.getInt(7));
        SpeechMainActivity.btn_player_pause.setVisibility(View.VISIBLE);
        SpeechMainActivity.btn_player_play.setVisibility(View.GONE);
    }


    @Override
    public void onDestroy() {
        // Don't forget to shutdown!
        if (mTts != null) {
            mTts.shutdown();
        }
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();


        sh.setCount(sh.getCount() + 1);
        Utility.checkThreeDaysAdvertisement(sh,this);

    }


    @Override
    public void onBackPressed() {

        if (!mLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        ExitDialog exitDialog = new ExitDialog(this, this.adobj);
        exitDialog.show();
        Window window = exitDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        App.getInstance(this).setClickedButton("Back", "back button", "native ad");
    }


    private void openAppToCopy() {
        CommonUtilities obj = new CommonUtilities();
        if (obj.appInstalledOrNot(sh.getOpenPackageName(), getApplicationContext())) {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(sh.getOpenPackageName());
            startActivity(launchIntent);
            Toast.makeText(getApplicationContext(), "Now copy large messages from Whatsapp to Read", Toast.LENGTH_LONG).show();

        } else {
            Dialogs object = new Dialogs(getApplicationContext(),this);
            object.appListDialog(this);
        }


    }



    private void updatePullUpLayout() {

        textView_selected_message.setText(cursor.getString(3));
        textView_bottom_date.setText(cursor.getString(5));
        editText_highlightingtext.setText(cursor.getString(2));
        CommonUtilities.speakingText = cursor.getString(2);
        String position=cursor.getPosition() + 1 + ".";
        textView_selected_message_position.setText(position);

    }

    private final GranularTextToSpeech.SingAlongListener mSingAlongListener = new GranularTextToSpeech.SingAlongListener() {
        @Override
        public void onSequenceStarted() {

            btn_player_pause.setVisibility(View.VISIBLE);
            btn_player_play.setVisibility(View.GONE);
        }

        @Override
        public void onUnitSelected(final int start, final int end) {
            if ((start < 0) || (end > CommonUtilities.speakingText.length())) {
                // The text changed while we were speaking.
                // TODO: We should be able to handle this.
                mTtsWrapper.stop();

                return;
            }
            editText_highlightingtext.requestFocus();
            editText_highlightingtext.setSelection(start, end);
        }

        @Override
        public void onSequenceCompleted() {


            playNextMessage();

        }
    };

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.fab) {
            Dialogs obj = new Dialogs(getApplicationContext(),this);
            obj.appListDialog(this);
            return true;
        }


        return false;
    }

    @Override
    public void languageButton(String language) {
     //   System.out.println("====== interface" + language);
    }

    @Override
    public void messageDeleted() {
     //   System.out.println("==========yes");
    }

    @Override
    public void messageAdded() {

    }

    @Override
    public void languageChanged() {
        this.mTts.setLanguage(mLocale);

    }

    @Override
    public void onDialogClick(int id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView_bottom_date.setText("Language : " + mLocale.getDisplayName());
            }
        });

    }


    /**
     * Transfers callbacks to the main thread.
     */
    private static class TypeAndSpeakHandler extends ReferencedHandler<SpeechMainActivity> {
        private static final int TTS_INITIALIZED = 1;
        private static final int DISMISS_DIALOG = 2;

        public TypeAndSpeakHandler(SpeechMainActivity parent) {
            super(parent);
        }

        @Override
        protected void handleMessage(android.os.Message msg, SpeechMainActivity parent) {
            switch (msg.what) {
                case TTS_INITIALIZED:
                    //   parent.onTtsInitialized(msg.arg1);
                    break;
                case DISMISS_DIALOG:
                    //  parent.mPinnedDialogManager.dismissPinnedDialog(msg.arg1);
                    break;
            }
        }


        public void transferOnTtsInitialized(int status) {
            obtainMessage(TTS_INITIALIZED, status, 0).sendToTarget();
        }

        public void dismissDialogDelayed(int id, long delay) {
            sendMessageDelayed(obtainMessage(DISMISS_DIALOG, id, 0), delay);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                openAppToCopy();
                break;

            case R.id.btn_player_play:
                if (mTtsWrapper != null) {
                    if (mTtsWrapper.isSpeaking()) {
                        mTtsWrapper.setSegmentFromCursor(editText_highlightingtext.getSelectionStart());
                        mTtsWrapper.resume();
                    } else {
                        speak();
                    }
                } else {
                    speak();
                }

                break;
            case R.id.btn_player_pause:
                if (mTtsWrapper != null) {
                    mTtsWrapper.pause();
                    isPaused = true;
                }

                break;
            case R.id.btn_player_forward:
                if (mTtsWrapper != null)
                    mTtsWrapper.next();
                break;
            case R.id.btn_player_rewind:
                if (mTtsWrapper != null)
                    mTtsWrapper.previous();
                break;
            case R.id.btn_player_next:
                if (SystemClock.elapsedRealtime() - mLastClickTime < 200) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                playNextMessage();
                break;
            case R.id.btn_bottom_language:
                new LanguageDialog(this,this).execute("");

                break;
            case R.id.btn_bottom_overflow:
                showPopup();
                break;
            case R.id.btn_player_previous:
                if (SystemClock.elapsedRealtime() - mLastClickTime < 200) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                playPreviousMessage();
                break;
            case R.id.btn_equalizer:
                Dialogs obj = new Dialogs(this,this);
                obj.pitchLayoutDialog(SpeechMainActivity.this);
                break;
            case R.id.btn_nightmode:
                CommonUtilities.nightmode = !CommonUtilities.nightmode;
                setupNightMode();
                break;
            case R.id.btn_ttssettings:
                Intent intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                break;
            case R.id.btn_activity:
                Intent i = new Intent(SpeechMainActivity.this,
                        MessageDescriptionActivity.class);
                i.putExtra("havePosition", true);
                startActivity(i);
                break;
            default:
                break;
        }

    }



    public void showPopup() {
        PopupMenu popup = new PopupMenu(this, btn_bottom_overflow);

        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.bottom_menu);

        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_action_edit:
                //  editText_highlightingtext.setFocusable(true);
                return true;
            case R.id.delete:

                return true;
            default:
                return false;
        }
    }


    private void setupNextMessageUI() {
        updatePullUpLayout();

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        // if progress = 13 -> value = 3 + (13 * 0.1) = 4.3
        int step = 1;
        int min = 7;
        int value = (int) (min + (progress * step));

        sh.setTextSize(value);
        editText_highlightingtext.setTextSize(value);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void playNextMessage() {
        if (commonUtilities.checkNextMessageAvailable()) {
            commonUtilities.setupNextMessage();
            setupNextMessageUI();
            editText_highlightingtext.setSelection(0, 0);
            if (mTtsWrapper != null && mTtsWrapper.isSpeaking()) {
                mTtsWrapper.pause();
                speak();
            } else {
                try {
                    mTtsWrapper.pause();
                } catch (Exception e) {

                }
                speak();
            }
        } else {
            // editText_highlightingtext.setSelection(0, 0);
            if (!mTts.isSpeaking()) {
            //    System.out.println("===this is cu;prit1");
                editText_highlightingtext.setSelection(0, 0);
                btn_player_pause.setVisibility(View.GONE);
                btn_player_play.setVisibility(View.VISIBLE);
            }
        }
    }

    private void playPreviousMessage() {
        if (commonUtilities.checkPreviousMessageAvailable()) {
            btn_player_pause.setVisibility(View.VISIBLE);
            btn_player_play.setVisibility(View.GONE);
            commonUtilities.setupPreviousMessage();
            setupNextMessageUI();
            editText_highlightingtext.setSelection(0, 0);

            //  mTtsWrapper.stop();
            if (mTtsWrapper != null && mTtsWrapper.isSpeaking()) {
                mTtsWrapper.pause();

                speak();
            } else {
                speak();
            }
            btn_player_pause.setVisibility(View.VISIBLE);
            btn_player_play.setVisibility(View.GONE);
        } else {

            if (!mTts.isSpeaking()) {
            //    System.out.println("===this is cu;prit2");
                btn_player_pause.setVisibility(View.GONE);
                btn_player_play.setVisibility(View.VISIBLE);
            }


        }
    }

    private void setupCursorPosition() {
        if (sh.getLastPlayedPosition() >= cursor.getCount()) {
            sh.setLastPlayedPosition(cursor.getCount() - 1);
        }

        cursor.moveToPosition(sh.getLastPlayedPosition());
        CommonUtilities.speakingText = cursor.getString(2);
    }

    private void setupNightMode() {
        if (CommonUtilities.nightmode) {
            btn_nightmode.setImageResource(R.drawable.ic_action_invertcolor);
            edt_scrollview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            editText_highlightingtext.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        } else {
            btn_nightmode.setImageResource(R.drawable.ic_action_invertcolor_off);
            edt_scrollview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            editText_highlightingtext.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        }
    }


    public class LanguageDialog extends AsyncTask<String, Void, Void> {

        Context context;
        DialogButtonClickCallBack dialogButtonClickCallBack;
        LanguageDialog(Context context,DialogButtonClickCallBack dialogButtonClickCallBack) {
            this.context = context;
            this.dialogButtonClickCallBack=dialogButtonClickCallBack;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(context);
            progress.setMessage("Please Wait");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setProgress(0);
            progress.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... voids) {


            Locale[] locales = Locale.getAvailableLocales();

            List<Locale> localeList = new ArrayList<>();

            List<String> stringList = new ArrayList<>();
            TreeMap<String, Locale> hi = new TreeMap<>();


            try {
                for (Locale locale : locales) {
                    int res = mTts.isLanguageAvailable(locale);
                    if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE) {

                        hi.put(locale.getDisplayName(), locale);

                    }
                }
                Dialogs obj = new Dialogs(getApplicationContext(),dialogButtonClickCallBack);

                Set set2 = hi.entrySet();
                Iterator iterator2 = set2.iterator();
                while (iterator2.hasNext()) {
                    Map.Entry me2 = (Map.Entry) iterator2.next();

                    localeList.add((Locale) me2.getValue());
                    stringList.add((String) me2.getKey());

                }
                obj.languageDialog(SpeechMainActivity.this, stringList, localeList, mTts, hi);

            } catch (MissingResourceException e) {

            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progress != null)
                        progress.cancel();
                }
            });

            super.onPostExecute(aVoid);
        }
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange <= 0) {
            //GAIN -> PLAY
        //    Log.d("=======", "loss pause");
            if (!isPaused) {
                if (mTtsWrapper != null && mTtsWrapper.isSpeaking()) {
                    mTtsWrapper.pause();
                    callInterruptFlag = true;
                }
            } else {
                callInterruptFlag = false;
            }


        } else {
            //GAIN -> PLAY

        //    Log.d("=======", "gain play");

            if (mTtsWrapper != null && mTtsWrapper.isSpeaking() && callInterruptFlag) {
                mTtsWrapper.resume();
                callInterruptFlag = false;
            }

        }
    }

    private void afterDelete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                if (mTtsWrapper != null && mTtsWrapper.isSpeaking()) {
                    mTtsWrapper.setListener(null);
                    mTtsWrapper.stop();
                }
                cursor = db.loadAllMessages();

                if (cursor.getCount() == 0) {
                    sh.setLastPlayedPosition(0);

                    clipobj.addFirstTimeMessageToTable(getApplicationContext());
                    cursor = db.loadAllMessages();
                }
                cursorAdapter.swapCursor(cursor);
                setupCursorPosition();

                setupNextMessageUI();

                editText_highlightingtext.setSelection(0, 0);
            }
        });
    }

    private void loadBanner() {

        AdView adView = new AdView(this);
        adView.setAdUnitId(getResources().getString(R.string.banner_ad_unit_id));
        adContainerView.addView(adView);

        AdRequest adRequest =
                new AdRequest.Builder()
                        //.addTestDevice("B16275C051562E8FBF875C10D04C629E")
                        .build();

        AdSize adSize = getAdSize();
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adContainerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                adContainerView.setVisibility(View.GONE);
                super.onAdFailedToLoad(loadAdError);
            }

           /* @Override
            public void onAdFailedToLoad(int i) {
                adContainerView.setVisibility(View.GONE);
            }*/
        });
    }
    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void loadNativeAd(){
        AdLoader.Builder builder = new AdLoader.Builder(this,getResources().getString(R.string.native_ad_close_app));
        builder.withAdListener(new AdListener(){
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError)
            {
                super.onAdFailedToLoad(loadAdError);
                System.out.println("==== native ad failed");
            }
        });
        builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                adobj = nativeAd;
                System.out.println("==== native ad loaded");
            }
        });
       /* builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener()
        {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd)
            {
                adobj = unifiedNativeAd;
                System.out.println("==== native ad loaded");
            }
        });*/
        AdLoader adLoader =builder.build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }
}
