package org.ifaco.friendtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Restarter extends BroadcastReceiver {
    SharedPreferences sp;

    @Override
    public void onReceive(Context c, Intent intent) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            sp = PreferenceManager.getDefaultSharedPreferences(c);
            Alarm.awaken(c);
        }
    }
}
