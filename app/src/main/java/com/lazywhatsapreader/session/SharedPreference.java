package com.lazywhatsapreader.session;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreference {
    SharedPreferences pref;

    private Editor editor;

    private Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "LazyReaderPref";
    private static SharedPreference instance = null;


    public SharedPreference(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public static SharedPreference getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreference(context);
        }
        return instance;
    }

    public void setlastmessage(String lastmessage) {
        editor.putString("lastmessage", lastmessage);
        editor.commit();
    }

    public String getlastmessage() {
        return pref.getString("lastmessage", "");
    }

    public void setOpenPackageName(String OpenPackageName) {
        editor.putString("OpenPackageName", OpenPackageName);
        editor.commit();
    }

    public String getOpenPackageName() {
        return pref.getString("OpenPackageName", "com.whatsapp");
    }

    public void setspeechrate(float speechrate) {
        editor.putFloat("speechrate", speechrate);
        editor.commit();
    }

    public float getspeechrate() {
        return pref.getFloat("speechrate", 1);
    }

    public void setpitch(float pitch) {
        editor.putFloat("pitch", pitch);
        editor.commit();
    }

    public float getpitch() {
        return pref.getFloat("pitch", 1);
    }


    public void setLastPlayedPosition(int lastposition) {
        editor.putInt("lastposition", lastposition);
        editor.commit();
    }

    public int getLastPlayedPosition() {
        return pref.getInt("lastposition", 0);

    }
	public void setIsFirstTime(boolean isFirstTime){
		editor.putBoolean("isFirstTime",isFirstTime);
		editor.commit();
	}
	public  boolean getIsFirstTime(){
		return  pref.getBoolean("isFirstTime",true);
	}

    public void setTextSize(int textSize) {
        editor.putInt("textsize", textSize);
        editor.commit();
    }

    public int getTextSize() {
        return pref.getInt("textsize", 14);
    }

    public void setCount(int Count) {
        editor.putInt("Count", Count);
        editor.commit();
    }

    public int getCount() {
        return pref.getInt("Count", 1);
    }

    public void setDate(String date) {
        editor.putString("date", date);
        editor.commit();
    }

    public String getDate() {
        return pref.getString("date", "");
    }
}
