package com.lazywhatsapreader.adapters;

/**
 * Created by gopinaths on 9/1/2017.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.lazywhatsapreader.R;
import com.lazywhatsapreader.common.CommonUtilities;
import com.lazywhatsapreader.session.SharedPreference;

import java.util.List;

public class AllListAdapter extends BaseAdapter {
    Context context;
    List<ApplicationInfo> appsList;
    private static LayoutInflater inflater = null;
    private PackageManager packageManager;
    private SharedPreference sh;
    AlertDialog dialog;
    Handler mHandler;

    public AllListAdapter(Context context, List<ApplicationInfo> appsList, SharedPreference sh, AlertDialog dialog) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.appsList = appsList;
        this.sh = sh;
        this.dialog = dialog;
        packageManager = context.getPackageManager();
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return appsList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder {
        TextView appName;
        ImageView iconview;
        ConstraintLayout fullView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.applist_child, null);
        holder.appName = rowView.findViewById(R.id.list_app_name);
        holder.iconview = rowView.findViewById(R.id.app_icon);
        holder.fullView = rowView.findViewById(R.id.fullView);
        ApplicationInfo applicationInfo = appsList.get(position);
        if (null != applicationInfo) {

            holder.appName.setText(applicationInfo.loadLabel(packageManager));

            mHandler = new Handler();

            new Thread(new Runnable() {
                @Override
                public void run() {

                    Drawable drawable=applicationInfo.loadIcon(packageManager);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            holder.iconview.setImageDrawable(drawable);
                        }
                    });
                }
            }).start();


        }
        if (applicationInfo.packageName.equalsIgnoreCase(sh.getOpenPackageName())) {
            holder.fullView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        } else {
            holder.fullView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }
        if (applicationInfo.packageName.equalsIgnoreCase(CommonUtilities.PACKAGE_NAME)) {
            holder.appName.setTextColor(ContextCompat.getColor(context, R.color.grey));

        }
        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (appsList.get(position).packageName.equalsIgnoreCase(CommonUtilities.PACKAGE_NAME)) {
                    Toast.makeText(context, "Please Choose some other App", Toast.LENGTH_LONG).show();
                    return;
                }

                sh.setOpenPackageName(appsList.get(position).packageName);
                dialog.dismiss();
            }
        });
        return rowView;
    }

}
