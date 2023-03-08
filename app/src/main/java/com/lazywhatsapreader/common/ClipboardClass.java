package com.lazywhatsapreader.common;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.lazywhatsapreader.App;
import com.lazywhatsapreader.R;
import com.lazywhatsapreader.pogo.Message;
import com.lazywhatsapreader.session.SharedPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ClipboardClass {
    private ClipboardManager clipboard;
    private SharedPreference sh;
    private String String_2_withoutdate, String_3_withoutSmiley, String_4_3lines;
    private DatabaseHandler db;
    private SharedPreferences preference;

    public void StartClipboard(final Context context, SharedPreferences pref) {
        sh = SharedPreference.getInstance(context);
        db = DatabaseHandler.getInstance(context);
        this.preference = pref;
        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(() -> {
            // TODO Auto-generated method stub
           clipboardChanged(context);
        });


    }

    public void clipboardChanged(Context context){
        if (clipboard.hasPrimaryClip()) {

            String tempsession = sh.getlastmessage();
            // Gets the clipboard data from the clipboard
            ClipData clip = clipboard.getPrimaryClip();
            // Gets the first item from the clipboard data
            ClipData.Item item = null;
            if (clip != null) {
                item = clip.getItemAt(0);
            }
            String atPresentinClipboard;
            if (item != null) {
                atPresentinClipboard = item.coerceToText(context).toString();
            } else {
                return;
            }

            int atleastword = (Integer.parseInt(preference.getString("atleastwordkey", "5")));


            if (!tempsession.equals(atPresentinClipboard)) {
                sh.setlastmessage(atPresentinClipboard);
                String_2_withoutdate = checkDateComingorNot(atPresentinClipboard);
                String_3_withoutSmiley = String_2_withoutdate;
                String_4_3lines = String_2_withoutdate.substring(0, Math.min(String_2_withoutdate.length(), 150));

                if (String_3_withoutSmiley.split("\\s+").length > atleastword) {
                    db.addMessage(new Message(atPresentinClipboard, String_2_withoutdate, String_3_withoutSmiley, String_4_3lines, giveCurrentTime(), 0));
                    App.getInstance(context).setClickedButton("Message", "clip", atPresentinClipboard);
                }

            }


        }
    }

    public void filterMessageText(int id,String latestEditedMessage){
        String String_2_withoutdate = checkDateComingorNot(latestEditedMessage);
        String String_3_withoutSmiley = String_2_withoutdate;
        String String_4_3lines = String_2_withoutdate.substring(0, Math.min(String_2_withoutdate.length(), 150));

            db.updateEditedMessageMessage(id,latestEditedMessage, String_2_withoutdate, String_3_withoutSmiley, String_4_3lines);
    }

    public String checkDateComingorNot(String temp) {
        String a[] = temp.split("\\r?\\n");

        StringBuffer sb = new StringBuffer("");
        ArrayList<String> mylist = new ArrayList<String>();

        for (String s : a) {

            String aOfI = s.toLowerCase();
            if (aOfI.contains("[") && aOfI.contains("]") && (aOfI.contains("am]")) || aOfI.contains("pm]") || aOfI.contains("p.m") || aOfI.contains("a.m")) {

                String b[] = s.split(": ");
                for (String value : b) {

                    String bOfJ = value.toLowerCase();

                    if (bOfJ.contains("[") && bOfJ.contains("]") && (bOfJ.contains("am]")) || bOfJ.contains("pm]") || bOfJ.contains("p.m") || bOfJ.contains("a.m")) {

                        mylist.add("\n");
                    } else {
                        sb.append(value);
                        sb.append("\n");
                        mylist.add(value);
                    }
                }

            } else {
                sb.append(s);
                sb.append("\n");
                mylist.add(s);
            }
        }
        return sb.toString();


    }


    public String giveCurrentTime() {
        SimpleDateFormat ft = new SimpleDateFormat("MMM d', 'hh:mm a");
        return ft.format(Calendar.getInstance().getTime());
    }

    public void addFirstTimeMessageToTable(Context context) {

        addToTable(context.getResources().getString(R.string.welcome_message1));
        addToTable(context.getResources().getString(R.string.welcome_message2));
        addToTable(context.getResources().getString(R.string.welcome_message3));
    }

    private void addToTable(String firstmessage) {
        ClipboardClass ob = new ClipboardClass();
        sh.setlastmessage(firstmessage);
        String_2_withoutdate = ob.checkDateComingorNot(firstmessage);
        String_3_withoutSmiley = String_2_withoutdate;
        String_4_3lines = String_2_withoutdate.substring(0, Math.min(String_2_withoutdate.length(), 150));
        db.addMessage(new Message(firstmessage, String_2_withoutdate, String_3_withoutSmiley, String_4_3lines, ob.giveCurrentTime(), 0));

    }


}
