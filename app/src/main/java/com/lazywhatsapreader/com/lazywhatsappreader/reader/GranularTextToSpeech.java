package com.lazywhatsapreader.com.lazywhatsappreader.reader;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.lazywhatsapreader.common.CommonUtilities;
import com.lazywhatsapreader.session.SharedPreference;
import com.vdurmont.emoji.EmojiParser;

import java.text.BreakIterator;
import java.util.HashMap;
import java.util.Locale;

import static com.lazywhatsapreader.activities.SpeechMainActivity.btn_player_pause;
import static com.lazywhatsapreader.activities.SpeechMainActivity.btn_player_play;

/**
 * A wrapper class for {@link TextToSpeech} that adds support for reading at a
 * given granularity level using a {@link BreakIterator}.
 */
public class GranularTextToSpeech extends Service {
    private static final int UTTERANCE_COMPLETED = 1;
    private static final int RESUME_SPEAKING = 2;

    private final CharSequenceIterator mCharSequenceIterator = new CharSequenceIterator(null);
    private final TextToSpeechStub mTts;
    private final HashMap<String, String> mParams;

    private BreakIterator mBreakIterator;
    private SingAlongListener mListener = null;
    private CharSequence mCurrentSequence = null;

    private int mUnitEnd = 0;
    private int mUnitStart = 0;

    private boolean mIsPaused = false;
    private SharedPreference sh;
    private SharedPreferences preference;
    public static Activity activity;
    /**
     * Flag that lets the utterance completion listener know whether to advance
     * automatically. Automatically resets after each completed utterance.
     */
    private boolean mBypassAdvance = false;

    public GranularTextToSpeech(Context context, TextToSpeech tts, Locale defaultLocale, SharedPreference sh, Activity activity) {
        this(context, new TextToSpeechWrapper(tts), defaultLocale);
        this.sh = sh;
        preference = PreferenceManager.getDefaultSharedPreferences(context);

        this.activity = activity;
    }

    public GranularTextToSpeech(Context context, TextToSpeechStub tts, Locale defaultLocale) {
        mTts = tts;

        mParams = new HashMap<>();
        mParams.put(Engine.KEY_PARAM_UTTERANCE_ID, "SingAlongTTS");

        if (defaultLocale != null) {
            mBreakIterator = BreakIterator.getSentenceInstance(defaultLocale);
        } else {
            mBreakIterator = BreakIterator.getSentenceInstance(Locale.US);
        }
    }

    public void setListener(SingAlongListener listener) {
        mListener = listener;
    }

    public void setLocale(Locale locale) {
        mBreakIterator = BreakIterator.getSentenceInstance(locale);

        // Reset the text since we had to recreate the break iterator.
        setText(mCurrentSequence);
    }

    public void setLocale(Locale locale, CharSequence newmCurrentSequence) {
        mBreakIterator = BreakIterator.getSentenceInstance(locale);

        // Reset the text since we had to recreate the break iterator.
        setText(newmCurrentSequence);
    }

    public void speak() {
        pause();

        mTts.setOnUtteranceCompletedListener(mOnUtteranceCompletedListener);

        if (mListener != null) {
            mListener.onSequenceStarted();
        }

        resume();
    }

    public void setText(CharSequence text) {
        mCurrentSequence = text;
        mUnitStart = 0;
        mUnitEnd = 0;
        mCharSequenceIterator.setCharSequence(mCurrentSequence);
        mBreakIterator.setText(mCharSequenceIterator);
    }

    public void pause() {
        mIsPaused = true;
        mTts.stop();
    }

    public void resume() {
        mIsPaused = false;
        onUtteranceCompleted(null);
    }

    public void next() {
        nextInternal();
        mBypassAdvance = !mIsPaused;
        mTts.stop();
    }

    public void previous() {
        previousInternal();
        mBypassAdvance = !mIsPaused;
        mTts.stop();
    }

    public boolean isSpeaking() {
        return (mCurrentSequence != null);
    }

    public void setSegmentFromCursor(int cursor) {
        if ((cursor >= mCurrentSequence.length()) || (cursor < 0)) {
            cursor = 0;
        }

        if (safeIsBoundary(mBreakIterator, cursor)) {
            mUnitStart = mBreakIterator.current();
            safeFollowing(mBreakIterator, cursor);
            mUnitEnd = mBreakIterator.current();
        } else {
            mUnitEnd = mBreakIterator.current();
            safePreceding(mBreakIterator, cursor);
            mUnitStart = mBreakIterator.current();
        }

        mBypassAdvance = true;

        if (mListener != null) {
            mListener.onUnitSelected(mUnitStart, mUnitEnd);
        }
    }

    public void stop() {
        mIsPaused = true;

        mTts.stop();
        mTts.setOnUtteranceCompletedListener(null);

        if (mListener != null) {
            mListener.onSequenceCompleted();
        }

        setText(null);

        mUnitStart = 0;
        mUnitEnd = 0;
    }

    /**
     * Move the break iterator forward by one unit. If the cursor is in the
     * middle of a unit, it will move to the next unit.
     *
     * @return {@code true} if the iterator moved forward or {@code false} if it
     * already at the last unit.
     */
    private boolean nextInternal() {
        do {
            final int result = safeFollowing(mBreakIterator, mUnitEnd);

            if (result == BreakIterator.DONE) {
                return false;
            }

            mUnitStart = mUnitEnd;
            mUnitEnd = mBreakIterator.current();
        } while (isWhitespace(mCurrentSequence.subSequence(mUnitStart, mUnitEnd)));

        if (mListener != null) {
            mListener.onUnitSelected(mUnitStart, mUnitEnd);
        }

        return true;
    }

    /**
     * Move the break iterator backward by one unit. If the cursor is in the
     * middle of a unit, it will move to the beginning of the unit.
     *
     * @return {@code true} if the iterator moved backward or {@code false} if
     * it already at the first unit.
     */
    private boolean previousInternal() {
        do {
            final int result = safePreceding(mBreakIterator, mUnitStart);

            if (result == BreakIterator.DONE) {
                return false;
            }

            mUnitEnd = mUnitStart;
            mUnitStart = mBreakIterator.current();
        } while (isWhitespace(mCurrentSequence.subSequence(mUnitStart, mUnitEnd)));

        if (mListener != null) {
            mListener.onUnitSelected(mUnitStart, mUnitEnd);
        }

        return true;
    }

    private static boolean isWhitespace(CharSequence text) {
        return TextUtils.getTrimmedLength(text) == 0;
    }

    private void onUtteranceCompleted(String utteranceId) {
        if (mCurrentSequence == null) {
            // Shouldn't be speaking now.
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("this is culprit 4");
                    btn_player_pause.setVisibility(View.GONE);
                    btn_player_play.setVisibility(View.VISIBLE);
                }
            });
            return;
        }

        if (mIsPaused) {
            // Don't move to the next segment if paused.
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("this is culprit 5");
                    // btn_player_pause.setVisibility(View.GONE);
                    // btn_player_play.setVisibility(View.VISIBLE);
                }
            });
            return;
        }

        if (mBypassAdvance) {
            mBypassAdvance = false;
        } else if (!nextInternal()) {
            stop();
            return;
        }

        speakCurrentUnit();
    }

    private void speakCurrentUnit() {
        if (mCurrentSequence.length() == 0) {
            return;
        }

        sanityCheck();

        final CharSequence text = mCurrentSequence.subSequence(mUnitStart, mUnitEnd);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn_player_pause.setVisibility(View.VISIBLE);
                btn_player_play.setVisibility(View.GONE);
            }
        });
        mTts.speak(text.toString(), TextToSpeech.QUEUE_FLUSH, mParams, sh,preference);
    }

    private void sanityCheck() {
        final int length = mCurrentSequence.length();

        if ((mUnitStart < 0) || (mUnitStart >= mCurrentSequence.length())) {
            throw new IndexOutOfBoundsException("Unit start (" + mUnitStart
                    + ") is invalid for string with length " + length);
        } else if ((mUnitEnd < 0) || (mUnitEnd > mCurrentSequence.length())) {
            throw new IndexOutOfBoundsException("Unit end (" + mUnitEnd
                    + ") is invalid for string with length" + length);
        }
    }

    private static boolean safeIsBoundary(BreakIterator iterator, int offset) {
        try {
            return iterator.isBoundary(offset);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static int safeFollowing(BreakIterator iterator, int offset) {
        try {
            return iterator.following(offset);
        } catch (IllegalArgumentException e) {
            return BreakIterator.DONE;
        }
    }

    private static int safePreceding(BreakIterator iterator, int offset) {
        try {
            return iterator.preceding(offset);
        } catch (IllegalArgumentException e) {
            return BreakIterator.DONE;
        }
    }

    private final SingAlongHandler mHandler = new SingAlongHandler(this);

    private final OnUtteranceCompletedListener mOnUtteranceCompletedListener = new OnUtteranceCompletedListener() {
        @Override
        public void onUtteranceCompleted(String utteranceId) {
            mHandler.obtainMessage(UTTERANCE_COMPLETED, utteranceId).sendToTarget();
           // System.out.println("=====ucompleted");

        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class SingAlongHandler extends ReferencedHandler<GranularTextToSpeech> {
        public SingAlongHandler(GranularTextToSpeech parent) {
            super(parent);
        }

        @Override
        protected void handleMessage(Message msg, GranularTextToSpeech parent) {
            switch (msg.what) {
                case UTTERANCE_COMPLETED:
                    parent.onUtteranceCompleted((String) msg.obj);
                    break;
                case RESUME_SPEAKING:
                    parent.resume();
                    break;
            }
        }
    }

    ;

    public interface TextToSpeechStub {
         void setOnUtteranceCompletedListener(
                OnUtteranceCompletedListener mOnUtteranceCompletedListener);

         int speak(String string, int queueFlush, HashMap<String, String> mParams, SharedPreference sh,SharedPreferences preferences);

         void stop();
    }

    private static class TextToSpeechWrapper implements TextToSpeechStub {
        private final TextToSpeech mTts;

        public TextToSpeechWrapper(TextToSpeech tts) {
            mTts = tts;

        }

        @SuppressWarnings("deprecation")
        @Override
        public void setOnUtteranceCompletedListener(OnUtteranceCompletedListener listener) {
            mTts.setOnUtteranceCompletedListener(listener);
        }

        @Override
        public int speak(String text, int queueMode, HashMap<String, String> params, SharedPreference sharedPreference,SharedPreferences preferences) {



            if(preferences.getBoolean("key_Url", false)){
                text=CommonUtilities.removeUrl(text);
            }
            if(preferences.getBoolean("key_number", false)){
                text=text.replaceAll("\\d", "");
            }
            if(preferences.getBoolean("key_Symbols", false)){
                text = text.replaceAll("[!@#_%^&*:€¢£¥₩₹$?✓√π÷×¶∆{};'+~()-]/", "");
            }
            text = EmojiParser.removeAllEmojis(text);
            //  text=text.replaceAll("[^\\dA-Za-z ]", "");
            //text = text.replaceAll("[!@#%^&*:+-]/", "");
            mTts.setPitch(sharedPreference.getpitch());
            mTts.setSpeechRate(sharedPreference.getspeechrate());
            return mTts.speak(text, queueMode, params);
        }

        @Override
        public void stop() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("this is culprit 3");
                    btn_player_pause.setVisibility(View.GONE);
                    btn_player_play.setVisibility(View.VISIBLE);
                }
            });
            mTts.stop();
        }
    }

    public interface SingAlongListener {
         void onSequenceStarted();

         void onUnitSelected(int start, int end);

         void onSequenceCompleted();
    }
}
