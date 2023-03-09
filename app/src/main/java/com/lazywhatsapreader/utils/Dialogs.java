package com.lazywhatsapreader.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lazywhatsapreader.R;
import com.lazywhatsapreader.activities.SpeechMainActivity;
import com.lazywhatsapreader.adapters.AllListAdapter;
import com.lazywhatsapreader.common.CommonUtilities;
import com.lazywhatsapreader.interfaces.DialogButtonClickCallBack;
import com.lazywhatsapreader.session.SharedPreference;

import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

/**
 * Created by gopinaths on 1/11/2017.
 */
public class Dialogs implements SeekBar.OnSeekBarChangeListener {
    private SharedPreference sh;
    private Context context;
    TextView txtRate, txtPitch, txtVolume;
    String player_rate, player_pitch, player_volume;
    AudioManager audiomanager;
    int step = 1;
    int max = 20;
    int min = 1;
    public static AlertDialog.Builder dialog;
    private String TAG = "aapp";
    private DialogButtonClickCallBack dialogButtonClickCallBack;


    public Dialogs(Context context) {
        this.context = context;
        sh = SharedPreference.getInstance(context);
        audiomanager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public Dialogs(Context context,DialogButtonClickCallBack dialogButtonClickCallBack) {
        this.context = context;
        this.dialogButtonClickCallBack=dialogButtonClickCallBack;
        sh = SharedPreference.getInstance(context);
        audiomanager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void languageDialog(final Activity activity, final List<String> list, final List<Locale> localelist, final TextToSpeech mTts, TreeMap<String, Locale> hi) {

        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(activity);
        // builderSingle.setIcon(R.drawable.ic_action_overflow);
        builderSingle.setTitle("Select Language:");


        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(list);
        builderSingle.setSingleChoiceItems(arrayAdapter, localelist.indexOf(SpeechMainActivity.mLocale), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(activity,
                        "Please play " + list.get(which) + " Language Messages", Toast.LENGTH_LONG).show();
                Locale newlocale = (Locale) localelist.get(which);

                mTts.setLanguage(newlocale);
                SpeechMainActivity.mLocale = newlocale;
                dialog.dismiss();
                dialogButtonClickCallBack.onDialogClick(1);

            }
        });

        builderSingle.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (SpeechMainActivity.progress != null)
                    SpeechMainActivity.progress.cancel();
                builderSingle.show();
            }
        });


    }

    public void pitchLayoutDialog(Activity activity) {
        // custom dialog
        dialog = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.pitch_layout, null);
        dialog.setView(dialogView);
        dialog.setTitle("Adjust Pitch, Rate and Volume");
        dialog.setCancelable(false);

        SeekBar seekBarRate, seekBarPitch, seekBarVolume;

        seekBarRate =  dialogView.findViewById(R.id.seekBarRate);
        seekBarPitch =  dialogView.findViewById(R.id.seekBarPitch);
        seekBarVolume =  dialogView.findViewById(R.id.seekBarVolume);
        txtRate =  dialogView.findViewById(R.id.textViewRate);
        txtPitch = dialogView.findViewById(R.id.textViewPitch);
        txtVolume =  dialogView.findViewById(R.id.textViewVolume);


        seekBarRate.setOnSeekBarChangeListener(this);
        seekBarPitch.setOnSeekBarChangeListener(this);
        seekBarVolume.setOnSeekBarChangeListener(this);

        player_rate = context.getResources().getString(R.string.player_rate);
        player_pitch = context.getResources().getString(R.string.player_pitch);
        player_volume = context.getResources().getString(R.string.player_volume);

        txtRate.setText(player_rate + sh.getspeechrate());
        txtPitch.setText(player_pitch + sh.getpitch());

        seekBarVolume.setMax(audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        int volume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);
        txtVolume.setText(player_volume + volume);

        seekBarVolume.setProgress(volume);
        int maxall = (max - min) / step;
        seekBarRate.setMax(maxall);

        seekBarRate.setProgress(((int) (sh.getspeechrate() * 10)));
        seekBarRate.invalidate();

        seekBarPitch.setMax(maxall);
        seekBarPitch.setProgress(((int) (sh.getpitch() * 10)));
        seekBarRate.invalidate();



        dialog.setNeutralButton("reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sh.setpitch(1);
                sh.setspeechrate(1);


            }
        });

        dialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        dialog.show();

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            switch (seekBar.getId()) {
                case R.id.seekBarRate:

                    int value = min + (progress * step);
                    sh.setspeechrate((float) value / 10);
                    txtRate.setText(player_rate + sh.getspeechrate());

                    break;
                case R.id.seekBarPitch:
                    int value2 = min + (progress * step);
                    sh.setpitch((float) value2 / 10);

                    txtPitch.setText(player_pitch + sh.getpitch());
                    break;
                case R.id.seekBarVolume:
                    txtVolume.setText(player_volume + progress);
                        audiomanager.setStreamVolume(
                                AudioManager.STREAM_MUSIC,
                                progress,
                                0);
                    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void appListDialog(Activity activity) {
        new LoadInstalledApplications(activity).execute();
    }

    private class LoadInstalledApplications extends AsyncTask<Void, Void, Void> {
        ListView lv;
        Activity activity;
        AlertDialog ad;
        PackageManager pm = context.getPackageManager();
        ProgressDialog progress;
        List<ApplicationInfo> packages;
        LoadInstalledApplications(Activity activity){
            this.activity=activity;
        }
        @Override
        protected void onPreExecute() {

            progress = ProgressDialog.show(activity, null,
                    "Loading applications... Please Wait");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //get a list of installed apps.
             packages = CommonUtilities.checkForLaunchIntent(pm.getInstalledApplications(PackageManager.GET_META_DATA), pm);

            /*for (ApplicationInfo packageInfo : packages) {
                Log.d(TAG, "Installed package :" + packageInfo.packageName);
                Log.d(TAG, "Source dir : " + packageInfo.sourceDir);
                Log.d(TAG, "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
            }*/
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {


            if (progress != null && progress.isShowing())
                progress.dismiss();

            dialog = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.applist_main, null);
            dialog.setView(dialogView);
            dialog.setTitle("Select your favourite app to open");
            dialog.setCancelable(true);

            lv =  dialogView.findViewById(R.id.list_applist);

            dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            });
            ad = dialog.create();
            ad.show();
            lv.setAdapter(new AllListAdapter(activity, packages, SharedPreference.getInstance(context), ad));
            super.onPostExecute(result);
        }

    }

}
