package org.ifaco.friendtracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import org.ifaco.friendtracker.etc.Navigator;

import java.util.Calendar;

import static org.ifaco.friendtracker.Fun.sp;
import static org.ifaco.friendtracker.Nav.btInterval;

public class Alarm {
    static int alarmType = AlarmManager.ELAPSED_REALTIME, alarmStart = -1;

    public static void awaken(Context c) {
        AlarmManager alarmMgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr == null) return;
        btInterval = sp.getLong(Nav.exBTNumber, Nav.defBT);
        boolean custom = btInterval != AlarmManager.INTERVAL_FIFTEEN_MINUTES && btInterval !=
                AlarmManager.INTERVAL_HALF_HOUR && btInterval != AlarmManager.INTERVAL_HOUR &&
                btInterval != AlarmManager.INTERVAL_HALF_DAY && btInterval != AlarmManager.INTERVAL_DAY;
        long start = SystemClock.elapsedRealtime();
        if (alarmStart > -1) {
            int subtraction = alarmStart;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int hourOfDay = subtraction / (60000 * 60);
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            subtraction -= hourOfDay;
            int minute = subtraction / 60000;
            calendar.set(Calendar.MINUTE, minute);
            start = calendar.getTimeInMillis();
        }
        if (custom) alarmMgr.setRepeating(alarmType, start, btInterval, sync(c));
        else alarmMgr.setInexactRepeating(alarmType, start, btInterval, sync(c));
    }

    static PendingIntent sync(Context c) {
        return PendingIntent.getBroadcast(c, 0, new Intent(c, Navigator.class), 0);
    }
}
